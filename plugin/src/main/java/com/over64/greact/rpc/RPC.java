package com.over64.greact.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.io.Reader;
import java.util.List;

public class RPC<T> {
    public @interface RPCEntryPoint {
        String value();
    }

    public interface Endpoint<T> {
        Object handle(T di, Gson gson, List<JsonElement> args);
    }

    static class Request {
        String endpoint;
        List<JsonElement> args;
    }

    Gson gson = new Gson();

    public String handle(T di, Reader in) throws Exception {
        var req = gson.fromJson(in, Request.class);
        var methodNamePos = req.endpoint.lastIndexOf(".");
        var className = req.endpoint.substring(0, methodNamePos);
        var methodName = req.endpoint.substring(methodNamePos + 1);
        var klass = Class.forName(className);
        for (var method : klass.getMethods())
            if (method.getName().equals(methodName)) {
                var result = method.invoke(null, di, gson, req.args);

                if (result == null)
                    return gson.toJson(JsonNull.INSTANCE);
                else
                    return gson.toJson(result, result.getClass());
            }

        throw new RuntimeException("unreachable");
    }
}
