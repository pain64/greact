package jstack.greact.uikit.routing;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.GReact;
import jstack.greact.dom.Globals;
import jstack.greact.dom.HTMLNativeElements;

public class Router implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
    record View(String href, HTMLNativeElements.Component0<HTMLNativeElements.div> slot){}
    final View[] views = new View[]{};

    private View findView() {
        String hash = JSExpression.of("window.location.hash"); // FIXME: add api to Globals.window
        for (var view : views)
            // FIXME: fix method shim
            if (JSExpression.of("hash.match(new RegExp(view.href)) != null")) return view;
        return null;
    }

    @Override public HTMLNativeElements.div mount() {
        HTMLNativeElements.div root = Globals.document.createElement("div");

        Runnable onLocationChange = () -> {
            var view = findView();
            var element = view != null ? view.slot : new HTMLNativeElements.div();
            root.innerHTML = "";
            GReact.mmount(root, element, new Object[]{});
        };

        onLocationChange.run();
        JSExpression.of("window.addEventListener('hashchange', onLocationChange)");
        return root;
    }
}
