package com.greact.di;

import com.greact.di.lib.Routine;
import javax.sql.DataSource;

public class DI {
    public record Data(DataSource commonDb, DataSource smsDb, Auth auth) {
        public <E extends Throwable> void bound(Routine<E> with) throws E {
            local.set(this);
            with.call();
        }
    }

    private static final ThreadLocal<Data> local = new ThreadLocal<>();

    protected static Data di() {
        return local.get();
    }
}
