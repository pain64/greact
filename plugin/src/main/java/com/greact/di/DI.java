package com.greact.di;

import com.greact.di.lib.PicoDI;
import com.greact.di.lib.PicoDI.Routine;
import javax.sql.DataSource;

public class DI {
    public record Data(DataSource commonDb, DataSource smsDb, Auth auth) { }
    private static final ThreadLocal<Data> local = new ThreadLocal<>();

    public static <E extends Throwable> void bound(Data data, Routine<E> with) throws E {
        PicoDI.bound(local, data, with);
    }

    public static Data di() {
        return PicoDI.di(local);
    }
}
