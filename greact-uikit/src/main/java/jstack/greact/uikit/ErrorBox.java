package jstack.greact.uikit;

import jstack.greact.dom.Globals;
import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.h3;
import jstack.greact.html.p;
import jstack.jscripter.transpiler.model.Require;
import org.jetbrains.annotations.Nullable;

@Require.CSS("error_box.css")
public class ErrorBox implements Component0<div> {
    private final String message;
    @Nullable private final String stacktrace;

    boolean opened = false;

    public ErrorBox(String message, @Nullable String stacktrace) {
        this.message = message;
        this.stacktrace = stacktrace;
    }

    @Override
    public Component0<div> mount() {
        return new div("uk__error-box_view-div") {{

            new div("uk__error-box_container") {{
                new div("uk__error-box_message-div") {{
                    new h3(message) {{
                        className = "uk__error-box_message-text";
                    }};
                }};

                new div("uk__error-box_buttons-div") {{
                    new div("uk__error-box_copy-button") {{
                        onclick = (ev) -> {
                            var text = stacktrace == null ? message : message + "\n" + stacktrace;
                            Globals.window.navigator.clipboard.writeText(text);
                        };
                    }};

                    if (stacktrace != null) {
                        new div("uk__error-box_stacktrace-button") {{
                            onclick = (ev) -> {
                                if (opened) effect(opened = false);
                                else effect(opened = true);
                            };
                        }};
                    }
                }};
            }};
            new div() {{
                if (!opened)
                    this.style.display = "none";

                new p(stacktrace) {{
                    className = "uk__error-box_stacktrace-text";
                }};
            }};
        }};
    }
}
