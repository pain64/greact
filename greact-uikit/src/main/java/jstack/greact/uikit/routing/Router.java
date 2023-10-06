package jstack.greact.uikit.routing;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.GReact;
import jstack.greact.dom.Globals;

public class Router implements Component0<div> {
    record View(String href, Component0<div> slot){}
    final View[] views = new View[]{};

    private View findView() {
        String hash = JSExpression.of("window.location.hash"); // FIXME: add api to Globals.window
        for (var view : views)
            // FIXME: fix method shim
            if (JSExpression.of("hash.match(new RegExp(view.href)) != null")) return view;
        return null;
    }

    @Override public div render() {
        div root = Globals.document.createElement("div");

        Runnable onLocationChange = () -> {
            var view = findView();
            var element = view != null ? view.slot : new div();
            root.innerHTML = "";
            GReact.mount(root, element, new Object[]{});
        };

        onLocationChange.run();
        JSExpression.of("window.addEventListener('hashchange', onLocationChange)");
        return root;
    }
}
