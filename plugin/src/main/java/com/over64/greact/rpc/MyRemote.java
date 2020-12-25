package com.over64.greact.rpc;

import java.util.function.Function;

public class MyRemote extends RPC<MyRemote.DI> {
    public static class DI {
        final String dependency;

        public DI(String dependency) {
            this.dependency = dependency;
        }
    }

    @RPCEntryPoint("/rpc") public static <T> T server(Function<DI, T> onServer) {
        throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
    }
}
