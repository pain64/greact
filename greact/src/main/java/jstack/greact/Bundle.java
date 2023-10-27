package jstack.greact;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Bundle {
    private final Map<String, Supplier<String>> resources;

    public Bundle(Map<String, Supplier<String>> resources) {
        this.resources = resources;
    }

    public String handleResource(
        String resourceName,
        Consumer<Integer> setStatusCode,
        Consumer<String> setContentType,
        OutputStream output
    ) throws IOException {
        var found = resources.get(resourceName);
        if (found != null) {
            if (resourceName.endsWith(".css"))
                setContentType.accept("text/css");
            if (resourceName.endsWith(".js"))
                setContentType.accept("text/javascript");

            setStatusCode.accept(200);
            output.write(found.get().getBytes());
        } else {
            setStatusCode.accept(404);
            output.write("not found".getBytes());
        }

        return "";
    }
}
