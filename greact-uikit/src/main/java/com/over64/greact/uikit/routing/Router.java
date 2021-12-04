package com.over64.greact.uikit.routing;

import com.greact.model.JSExpression;
import com.over64.greact.dom.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;

import static com.over64.greact.dom.Globals.document;

public class Router implements Component0<div> {
    record View(String href, Component0<div> slot){}

    final View[] views = new View[]{};

    private View findView() {
        String hash = JSExpression.of("window.location.hash"); // FIXME: add api to Globals.window
        for (var view : views)
            if (view.href == hash) return view;
        return views[0];
    }

    @Override
    public div mount() {
        div root = document.createElement("div");

        GReact.AsyncRunnable onLocationChange = () -> {
            var view = findView();
            root.innerHTML = "";
            GReact.mmount(root, view.slot, new Object[]{});
        };

        onLocationChange.run();
        JSExpression.of("window.addEventListener('hashchange', onLocationChange)");
        return root;
    }
}
