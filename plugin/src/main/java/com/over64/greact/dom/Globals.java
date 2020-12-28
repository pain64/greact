package com.over64.greact.dom;

import com.greact.model.JSExpression;
import com.greact.model.async;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");
    public static HtmlElement gReactElement;

    public static <T extends HtmlElement> void gReactMount(T dest, HTMLNativeElements.Component0<T> element, Object... args) {
        gReactElement = dest;
        JSExpression.of("element instanceof Function ? element(...args) : element.mount(...args)");
    }

    public static <T extends HtmlElement> T gReactReturn(Fragment.Renderer renderer) {
        renderer.render();
        return null;
    }

    @async public static <T> T doRemoteCall(String url, String endpoint, Object... args) {
        return JSExpression.of("""
            await (await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ endpoint: endpoint, args: args})
            })).json()""");
    }
}
