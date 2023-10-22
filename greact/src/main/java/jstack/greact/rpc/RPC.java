package jstack.greact.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jstack.jscripter.transpiler.model.RPCEndPoint;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Reader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RPC<T> {


    @Retention(RetentionPolicy.RUNTIME)
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
    private final boolean isRelease;
    private final ConcurrentHashMap<String, Method> cache;

    public RPC() {
        throw new RuntimeException("will be delegated to RPC(appBasePackage)");
    }

    public RPC(String appBasePackage) {
        this.appBasePackage = appBasePackage;
        this.cache = new ConcurrentHashMap<>();
        var files = System.getProperty("java.class.path").split(":");
        this.isRelease = getClass().getResourceAsStream("/bundle/.release") != null;
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
        var fullName = className + "." + methodName;
        if (!className.startsWith(appBasePackage)) throw new RuntimeException("unreachable");

        if (isRelease && cache.containsKey(fullName)) {
            var method = cache.get(fullName);
            var result = method.invoke(null, di, mapper, req.args);
            return mapper.writeValueAsString(result);
        }
        // FIXME: classloader leak
        var classLoader = !isRelease
            ? new RPCClassLoader(getClass().getClassLoader(), urls, appBasePackage)
            : getClass().getClassLoader();

        var klass = classLoader.loadClass(className);
        //var klass = Class.forName(className);

        for (var method : klass.getMethods())
            if (method.getName().equals(methodName)) {
                if (method.getAnnotation(RPCEndPoint.class) == null)
                    throw new RuntimeException("unreachable");
                method.setAccessible(true);
                var result = method.invoke(null, di, mapper, req.args);
                if (isRelease) cache.put(fullName, method);

                return mapper.writeValueAsString(result);
            }

        throw new RuntimeException("unreachable");
    }

    public record RPCError(String msg, @Nullable String stackTrace) { }

    public static String rpcErrorJson(
        String msg, @Nullable String stackTrace
    ) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(new RPCError(msg, stackTrace));
    }
}
