package com.over64.greact.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Reader;
import java.util.List;

public class RPC<T> {
    public @interface RPCEntryPoint {
        String value();
    }

    public interface Endpoint<T> {
        Object handle(T di, ObjectMapper mapper, List<JsonNode> args);
    }

    public static class Request {
        public String endpoint;
        public List<JsonNode> args;
    }

    ObjectMapper mapper = new ObjectMapper();

    public String handle(T di, Reader in) throws Exception {
        var req = mapper.readValue(in, Request.class);
        var methodNamePos = req.endpoint.lastIndexOf(".");
        var className = req.endpoint.substring(0, methodNamePos);
        var methodName = req.endpoint.substring(methodNamePos + 1);
        var klass = Class.forName(className);
        for (var method : klass.getMethods())
            if (method.getName().equals(methodName)) {
                var result = method.invoke(null, di, mapper, req.args);
                return mapper.writeValueAsString(result);
            }

        throw new RuntimeException("unreachable");
    }

    public record  RPCError(String error) {}
    public static String rpcErrorJson(String error) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(new RPCError(error));
    }
}
