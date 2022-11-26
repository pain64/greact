package jstack.greact.uikit.routing;

import jstack.greact.uikit.Array;
import jstack.greact.dom.HTMLNativeElements;

public class Route implements HTMLNativeElements.Component0<HTMLNativeElements.a> {
    final String _href;
    final String caption;

    public Route(Router router, String href, String caption, HTMLNativeElements.Component0<HTMLNativeElements.div> view) {
        this._href = href;
        this.caption = caption;
        Array.push(router.views, new Router.View(href, view));
    }

    @Override public HTMLNativeElements.a mount() {
        return new HTMLNativeElements.a() {{
            innerText = caption;
            href = _href;
        }};
    }
}
