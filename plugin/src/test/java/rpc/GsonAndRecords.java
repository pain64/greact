package rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class GsonAndRecords {
    record Foo(int x, String y) {}

    @Test
    void foo() {
        var gson = new Gson();
        var d = gson.toJson(new Foo(42, "hello"));
        gson.fromJson(d, Foo.class);

    }

    static record Req(String endpoint, List<JsonNode> args){}

    @Test
    void jackson() throws JsonProcessingException {
        var om = new ObjectMapper();
        var d = om.writeValueAsString(new Req("/rpc", List.of(
            BooleanNode.TRUE,
            new IntNode(42)
        )));

        var parsed = om.readValue(d, Req.class);

        parsed.args.get(0).asBoolean();
    }
}

