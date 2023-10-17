package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.span;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("spinner.css")
public class Spinner implements Component0<div> {
    @Override public div render() {
        return new div() {{
            className = "spinner";
            new span("working") {{ className = "spinner-span"; }};
            new div() {{ className = "bounce1"; }};
            new div() {{ className = "bounce2"; }};
            new div() {{ className = "bounce3"; }};
        }};
    }
}
