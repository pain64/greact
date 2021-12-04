package com.over64.greact.uikit.routing;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.uikit.Array;

public class Link {
    private final String path;

    public Link(Router router, String path, HTMLNativeElements.Component1<HTMLNativeElements.div, Long> view) {
        this.path = path;

        // FIXME: string escaping
        Array.push(router.views, new Router.View(path + "/\\\\d+", () -> {
            String[] hashPaths = JSExpression.of("window.location.hash.split('/')");
            var param = Array.last(hashPaths);
            var lParam = JSExpression.<Long>of("parseInt(param)");

            return new HTMLNativeElements.div() {{
                new HTMLNativeElements.slot<>(view, lParam);
            }};
        }));
    }

    public String get(long param) {return path + "/" + param;}
}
