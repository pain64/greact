package com.over64.greact.uikit.routing;

import com.greact.model.JSExpression;
import com.over64.greact.dom.GReact;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.div;

public class Router implements Component0<div> {
    final RouterView[] views;
    RouterView view;

    public Router(RouterView[] views) {this.views = views;}

    RouterView findView() {
        String hash = JSExpression.of("window.location.hash"); // FIXME: add api to Globals.window
        for (var view : views)
            if (view.href == hash) return view;
        return views[0];
    }

    @Override
    public div mount() {
        var root = (div) GReact.element;

        Runnable onLocationChange = () -> {
            view = findView();
            root.innerHTML = "";
            GReact.mount(root, view.slot, new Object[]{});
        };

        onLocationChange.run();
        JSExpression.of("window.addEventListener('hashchange', onLocationChange)");
        return null;
    }
}
