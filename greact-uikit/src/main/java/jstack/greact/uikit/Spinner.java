package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.Require;
import jstack.greact.dom.HTMLNativeElements;

@Require.CSS("spinner.css")
public class Spinner implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
    @Override public HTMLNativeElements.div mount() {
        return new HTMLNativeElements.div() {{
            className = "spinner";
            new HTMLNativeElements.span("working") {{ className = "spinner-span"; }};
            new HTMLNativeElements.div() {{ className = "bounce1"; }};
            new HTMLNativeElements.div() {{ className = "bounce2"; }};
            new HTMLNativeElements.div() {{ className = "bounce3"; }};
        }};
    }
}
