package greact.sample.server;

import java.util.Collections;
import java.util.List;

public class TypesafeSql {
    public static class Param {
        String name;
        Object value;
    }

    static Param param(String name, Object value) {
        return new Param() {{
            this.name = name;
            this.value = value;
        }};
    }

    <T> List<T> list(String stmt, Class<T> klass) {
        return Collections.EMPTY_LIST;
    }

    void exe(String stmt, Object value) {

    }

}
