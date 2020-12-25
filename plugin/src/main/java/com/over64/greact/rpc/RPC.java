package com.over64.greact.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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
        var handler = ((Class<Endpoint<T>>) Class.forName(req.endpoint)).newInstance();
        var result = handler.handle(di, gson, req.args);
        return gson.toJson(result, result.getClass());
    }
}
