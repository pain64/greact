package com.over64.greact.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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

    private final String appBasePackage;
    private final URL[] urls;

    public RPC(String appBasePackage) {
        this.appBasePackage = appBasePackage;

        var files = System.getProperty("java.class.path").split(":");
        urls = Arrays.stream(files).map(p -> {
            try {
                return new File(p).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
    }

    ObjectMapper mapper = new ObjectMapper();

    public String handle(T di, Reader in) throws Exception {
        var req = mapper.readValue(in, Request.class);
        var methodNamePos = req.endpoint.lastIndexOf(".");
        var className = req.endpoint.substring(0, methodNamePos);
        var methodName = req.endpoint.substring(methodNamePos + 1);

        var classLoader = new RPCClassLoader(RPC.class.getClassLoader(), urls, appBasePackage);
        var klass = classLoader.loadClass(className);
        //var klass = Class.forName(className);

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
