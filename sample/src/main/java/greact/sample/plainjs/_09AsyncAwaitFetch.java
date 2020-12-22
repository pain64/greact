package greact.sample.plainjs;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

public class _09AsyncAwaitFetch implements Component0<ul> {
    @async <T> T get(String url) {
        return JSExpression.of("""
            (await (await fetch(url)).json())""");
    }

    @Override public ul mount() {
        _09Todo[] list = get("https://jsonplaceholder.typicode.com/todos");

        return new ul() {{
            for (var x : list)
                new li() {{
                    new a("id: " + x.id + " title: " + x.title);
                }};
        }};
    }
}
