package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLElement.KeyHandler;
import jstack.greact.dom.HTMLNativeElements.input;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;

public class UKInput<T> extends Control<T> {
    public String className_;
    public String placeholder_;
    public KeyHandler onkeyup_;

    @Override
    public Component0<div> mount() {
        return new div() {{
            new input() {{
                if (className_ != null) className += " form-control";
                else className = "form-control";

                this.placeholder = placeholder_;
                this.onkeyup = onkeyup_;
            }};
        }};
    }
    @Override
    public Control<?> child() {
        return null;
    }
}
