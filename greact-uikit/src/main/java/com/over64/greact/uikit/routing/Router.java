package com.over64.greact.uikit.routing;

import com.greact.model.JSExpression;
import com.over64.greact.dom.GReact;
import com.over64.greact.dom.Globals;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.Window;
import com.over64.greact.uikit.Array;
import com.over64.greact.uikit.Dates;

import static com.over64.greact.dom.Globals.document;

public class Router implements Component0<div> {
    final RouterView[] views;
    RouterView view;

    public Router(RouterView[] views) {this.views = views;}

    @FunctionalInterface public interface LinkHolder {
        String get(long param);
    }

    public static LinkHolder link(RouterView[] routes, String path, Component1<div, Long> view) {
        Array.push(routes, new RouterView(path, () -> {
            String[] hashPaths = JSExpression.of("window.location.hash.split('/')");
            var param = Array.last(hashPaths);
            var lParam = JSExpression.<Long>of("parseInt(param)");

            return new div() {{
                new slot<>(view, lParam);
            }};
        }));

        return param -> path + "/" + param;
    }

    RouterView findView() {
        String hash = JSExpression.of("window.location.hash"); // FIXME: add api to Globals.window
        for (var view : views)
            if (view.href == hash) return view;
        return views[0];
    }

    @Override
    public div mount() {
        div root = document.createElement("div");

        GReact.AsyncRunnable onLocationChange = () -> {
            view = findView();
            root.innerHTML = "";
            GReact.mmount(root, view.slot, new Object[]{});
        };

        onLocationChange.run();
        JSExpression.of("window.addEventListener('hashchange', onLocationChange)");
        return root;
    }
}
