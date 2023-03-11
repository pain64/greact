package jstack.greact;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import jstack.jscripter.transpiler.TranspilerPlugin;
import org.apache.commons.cli.CommandLine;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Set;

public class GReactPluginW {
    Context context;
    CommandLine cmd;

//    record Cleanup(ClassLoader cl, SafeSqlChecksRunner checksRunner) implements Runnable {
//        @Override public void run() {
//            try {
//                System.out.println("CLEANUP SSQL CONNECTION & THREAD POOL");
//                if (checksRunner != null) checksRunner.close();
//                System.out.println("CLEANUP GREACT CACHED CLASSLOADER");
//                if (cl instanceof URLClassLoader ucl) ucl.close();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    public void init(JavacTask task, String[] strings) {
        System.out.println("INIT GREACT PLUGIN");
        //System.out.println("CURRENT_CL2: " + this.getClass().getClassLoader());
        context = ((BasicJavacTask) task).getContext();
        cmd = TranspilerPlugin.getCmd(strings);

//        Cleaner.create().register(
//            this, new Cleanup(
//                this.getClass().getClassLoader(), checksRunner
//            )
//        );

        task.addTaskListener(new TaskListener() {
            @Override public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();
                    var t0 = System.currentTimeMillis();

                    if (cmd.hasOption("tsql-check-enabled") &&
                        cmd.getOptionValue("tsql-check-enabled").equals("true")
                    )
                        new SafeSqlPlugin(
                            context, cmd, SafeSqlChecksRunner.instance(cmd)
                        ).apply(cu);

                    var t1 = System.currentTimeMillis();

                    new CodeViewPlugin(context).apply(cu);
                    var t2 = System.currentTimeMillis();

                    new RPCPlugin(context).apply(cu);
                    var t3 = System.currentTimeMillis();

                    new MarkupPlugin2(context).apply(cu);
                    var t4 = System.currentTimeMillis();

                    if (cmd.hasOption("greact-debug-patched-ast")) {
                        var env = JavacProcessingEnvironment.instance(context);
                        try {
                            var jsFile = env.getFiler().createResource(
                                StandardLocation.CLASS_OUTPUT,
                                cu.getPackageName().toString(),
                                e.getTypeElement().getSimpleName() + ".java.patch"
                            );

                            var writer = jsFile.openWriter();
                            writer.write(cu.toString());
                            writer.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    var t5 = System.currentTimeMillis();

                    System.out.println("[" + e.getCompilationUnit().getSourceFile().getName() + "]" +
                        "\nssql_checker   : " + (t1 - t0) + "ms" +
                        "\ncode_view      : " + (t2 - t1) + "ms" +
                        "\nrpc            : " + (t3 - t2) + "ms" +
                        "\nmarkup         : " + (t4 - t3) + "ms" +
                        "\ndebug_file     : " + (t5 - t4) + "ms"
                    );

                }
            }
        });

        new TranspilerPlugin().init(task, strings);
    }

    public static void close() {
        System.out.println("CLEANUP GREACT CLASSES");
        SafeSqlChecksRunner.closeAll();
        try {
            ((URLClassLoader) GReactPluginW.class.getClassLoader()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean doAnnotationProcessing(
        Set<? extends TypeElement> set, RoundEnvironment roundEnv
    ) {
        return new SafeSqlPlugin(context, cmd, SafeSqlChecksRunner.instance(cmd))
            .generateDto(set, roundEnv);
    }
}
