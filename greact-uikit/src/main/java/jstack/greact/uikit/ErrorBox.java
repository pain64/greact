package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.p;
import jstack.jscripter.transpiler.model.Require;
import org.jetbrains.annotations.Nullable;

import static jstack.greact.dom.Globals.window;

@Require.CSS("error_box.css")
public class ErrorBox implements Component0<div> {
    private final String message;
    @Nullable private final String stackTrace;

    private boolean showTrace = false;

    public ErrorBox(String message, @Nullable String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }

    @Override public Component0<div> mount() {
        return new div("uk__error-box") {{
            new div(); // for alignment
            new div() {{
                new p(message) {{
                    className = "uk__error-box_message-text";
                }};

                if (showTrace)
                    new p(stackTrace) {{
                        className = "uk__error-box_stacktrace-text";
                    }};
            }};

            new div("uk__error-box_buttons-div") {{
                if (stackTrace != null)
                    new div("uk__error-box_stacktrace-button") {{
                        if (showTrace)
                            className += " uk__error-box_button-active";

                        title = "Показать stacktrace";
                        onclick = ev -> effect(showTrace = !showTrace);
                    }};

                new div("uk__error-box_copy-button") {{
                    title = "Скопировать в буффер обмена";
                    onclick = ev -> window.navigator.clipboard.writeText(
                        stackTrace == null ? message : message + "\n" + stackTrace
                    );
                }};
            }};
        }};
    }
}
