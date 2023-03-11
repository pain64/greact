package jstack.greact;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.cli.CommandLine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class SafeSqlChecksRunner implements AutoCloseable {
    private static final ConcurrentHashMap<Parameters, SafeSqlChecksRunner> instances =
        new ConcurrentHashMap<>();

    record Parameters(
        String url, String username, String password, String driverClassName
    ) { }

    public static SafeSqlChecksRunner instance(CommandLine cmd) {
        Function<String, String> requireOption = name -> {
            if (!cmd.hasOption(name)) throw
                new RuntimeException("expected option " + name + " to be set");
            return cmd.getOptionValue(name);
        };

        var instanceKey = new Parameters(
            requireOption.apply("tsql-check-schema-url"),
            requireOption.apply("tsql-check-schema-username"),
            requireOption.apply("tsql-check-schema-password"),
            requireOption.apply("tsql-check-driver-classname")
        );

        return instances.computeIfAbsent(instanceKey, SafeSqlChecksRunner::new);
    }

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        2, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
    );

    final HikariDataSource ds;

    protected SafeSqlChecksRunner(Parameters parameters) {
        System.out.println(
            "SAFE_SQL CREATE CONNECTION FOR" +
                "\n    URL = " + parameters.url +
                "\n    USERNAME = " + parameters.username +
                "\n    PASSWORD = *****" +
                "\n    DRIVER CLASS NAME = " + parameters.driverClassName
        );

        this.ds = new HikariDataSource() {{
            setDriverClassName(parameters.driverClassName);
            setJdbcUrl(parameters.url);
            setUsername(parameters.username);
            setPassword(parameters.password);
            setMaximumPoolSize(8);
            setConnectionTimeout(10000);
        }};
    }

    interface CheckTask<T> {
        T check(Connection connection);
    }

    public <T> List<T> run(List<CheckTask<T>> tasks) {
        var futures = new ArrayList<CompletableFuture<T>>();
        var results = new ArrayList<T>();

        for (var task : tasks) {
            var future = CompletableFuture.supplyAsync(() -> {
                var t0 = System.currentTimeMillis();
                try (var connection = ds.getConnection()) {
                    var t1 = System.currentTimeMillis();
                    System.out.println("### GET CONNECTION TOOK: " + (t1 - t0) + "ms");

                    var result = task.check(connection);

                    var t2 = System.currentTimeMillis();
                    System.out.println("### APPLY CHECK TOOK: " + (t2 - t1) + "ms");

                    return result;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, executor);

            futures.add(future);
        }

        var errors = new ArrayList<Throwable>();
        for (var future : futures)
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                errors.add(e.getCause());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        if (!errors.isEmpty())
            if (errors.get(0) instanceof RuntimeException rt) throw rt;
            else throw new RuntimeException(errors.get(0));

        return results;
    }

    @Override public void close() throws Exception {
        ds.close();
        executor.shutdownNow();
        var __ = executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    public static void closeAll() {
        instances.forEachValue(0, instance -> {
            try {
                instance.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
