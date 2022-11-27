package util;

import jstack.greact.rpc.RPC;

import java.util.function.Function;

public class TestServer extends RPC<Void> {
    public TestServer() {super("");}
    @RPCEntryPoint("/rpc") public static <T> T server(Function<Void, T> onServer) {
        throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
    }
}
