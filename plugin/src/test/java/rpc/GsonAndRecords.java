package rpc;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

public class GsonAndRecords {
    record Foo(int x, String y) {}
    @Test  void foo() {
        var gson = new Gson();
        var d = gson.toJson(new Foo(42, "hello"));
        gson.fromJson(d, Foo.class);

    }
}
