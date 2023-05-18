package jstack.greact.uikit.controls;

import jstack.greact.html.div;
import jstack.greact.html.input;
import jstack.greact.html.label;
import jstack.greact.html.span;
import jstack.jscripter.transpiler.model.JSExpression;

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

    @Override public div mount() {
        var self = this;

        return new div() {{
            new span(label + " ");
            for (var variant : variants) {
                new label() {{
                    new input() {{
                        type = "radio";
                        checked = self.value == variant.value;
                        onchange = ev -> {
                            self.value = variant.value;
                            onReadyChanged.run();
                        };
                    }};
                    new span(" " + variant.label + " ");
                }};
            }
        }};
    }
}
