package jstack.greact.uikit.routing;

import jstack.greact.dom.HTMLNativeElements.Component1;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.slot;
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
            var lParam = JSExpression.<Long>of("parseInt(param)");

            return new div() {{
                new slot<>(view, lParam);
            }};
        }));
    }

    public String get(long param) {return path + "/" + param;}
}
