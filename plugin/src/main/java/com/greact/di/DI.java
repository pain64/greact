package com.greact.di;

import com.greact.di.lib.Routine;

import javax.sql.DataSource;
import java.util.Optional;

public class DI {
    public record Data(DataSource commonDb, DataSource smsDb, Auth auth) {
        public <E extends Throwable> void bound(Routine<E> with) throws E {
            try {
                local.set(this);
                with.call();
            } finally {
                local.remove();
            }
        }
    }

    private static final ThreadLocal<Data> local = new ThreadLocal<>();

    protected static Data di() {
        return Optional.ofNullable(local.get())
            .orElseThrow(() -> new IllegalStateException("thread-local di context not bound"));
    }
}
