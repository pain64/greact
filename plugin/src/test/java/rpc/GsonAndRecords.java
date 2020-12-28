package rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

public class GsonAndRecords {
    record Foo(int x, String y) {}

    @Test
    void foo() {
        var gson = new Gson();
        var d = gson.toJson(new Foo(42, "hello"));
        gson.fromJson(d, Foo.class);

    }

    @Test
    void jackson() throws JsonProcessingException {
        var om = new ObjectMapper();
        var d = om.writeValueAsString(new Foo(42, "hello"));

        om.readValue(d, Foo.class);
    }
}

