package util;

import jstack.greact.rpc.RPC;

import java.util.function.Function;

public class TestServer extends RPC<Void> {
    public static class DI {
        public int method() { return 42; }
    }
    public TestServer() { super(""); }
    @RPCEntryPoint("/rpc") public static <T> T server(Function<DI, T> onServer) {
        throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
    }
}
