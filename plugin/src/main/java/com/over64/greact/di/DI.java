package com.over64.greact.di;

import com.over64.greact.di.lib.PicoDI;
import org.sql2o.Connection;

import javax.sql.DataSource;

public class DI {
    public static record Conn(Connection sql2oConn) {
        public <T> T[] exe(String query, Class<T> klass) {
            return (T[]) sql2oConn.createQuery(query).executeAndFetch(klass).toArray();
        }
    }
    public static class CommonDb {
        public <T> T[] list(String query) {
            //return (T[]) sql2oConn.createQuery(query).executeAndFetch(klass).toArray();
            return null;
        }
        public <T1, T2> Tuple2<T1, T2>[] list2(String query) {
            return null;
        }
        public static class Tuple2<T1, T2> {
            public T1 f1;
            public T2 f2;
        }
    }
    public static class Data {
        public final CommonDb commonDb;
        public final DataSource smsDb;
        public final Auth auth;

        public Data(CommonDb commonDb, DataSource smsDb, Auth auth) {
            this.commonDb = commonDb;
            this.smsDb = smsDb;
            this.auth = auth;
        }
    }
//        public <T> T withConnection(Function<Conn, T> action) {
//            try (var conn = new Sql2o(commonDb).open()) {
//                return action.apply(new Conn(conn));
//            }
//        }

    private static final ThreadLocal<Data> local = new ThreadLocal<>();

    public static <E extends Throwable> void bound(Data data, PicoDI.Routine<E> with) throws E {
        PicoDI.bound(local, data, with);
    }

    public static Data di() {
        return PicoDI.di(local);
    }
}
