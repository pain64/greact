package com.over64.greact.uikit.routing;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Array;

public class Route implements Component0<a> {
    final String _href;
    final String caption;

    public Route(Router router, String href, String caption, Component0<div> view) {
        this._href = href;
        this.caption = caption;
        Array.push(router.views, new Router.View(href, view));
    }

    @Override public a mount() {
        return new a() {{
            innerText = caption;
            href = _href;
        }};
    }
}
