package jstack.greact.uikit.routing;

import jstack.greact.html.Component1;
import jstack.greact.html.div;
import jstack.greact.html.slot;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.uikit.Array;

public class Link {
    private final String path;

    public Link(Router router, String path, Component1<div, Long> view) {
        this.path = path;

        // FIXME: string escaping
        Array.push(router.views, new Router.View(path + "/\\\\d+", () -> {
            String[] hashPaths = JSExpression.of("window.location.hash.split('/')");
            var param = Array.last(hashPaths);
            var lParam = JSExpression.<Long>of("parseInt(:1)", param);

            return new div() {{
                new slot<>(view, lParam);
            }};
        }));
    }

    public String get(long param) {return path + "/" + param;}
}
