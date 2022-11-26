package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.HTMLNativeElements;

public class Radio<T> extends Control<T> {
    public record Variant<T>(String label, T value){}

    final Variant<T>[] variants;

    @SafeVarargs public Radio(String label, Variant<T>... variants) {
        this.label = label;
        this.variants = JSExpression.of("Array.from(arguments).slice(1)");
        if(this.variants.length > 0) {
            this.value = this.variants[0].value;
            this.ready = true;
            onReadyChanged.run();
        }
    }

    @Override public Control<T> child() {return null;}

    @Override public HTMLNativeElements.div mount() {
        var self = this;

        return new HTMLNativeElements.div() {{
            new HTMLNativeElements.span(label + " ");
            for (var variant : variants) {
                new HTMLNativeElements.label() {{
                    new HTMLNativeElements.input() {{
                        type = "radio";
                        checked = self.value == variant.value;
                        onchange = ev -> {
                            self.value = variant.value;
                            onReadyChanged.run();
                        };
                    }};
                    new HTMLNativeElements.span(" " + variant.label + " ");
                }};
            }
        }};
    }
}
