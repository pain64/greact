package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.over64.greact.model.components.DomProperty;
import com.over64.greact.model.components.Slot;

public class HTMLNativeElements {

    @JSNativeAPI public static class h1 extends HtmlElement {
        public h1() {};
        public h1(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static  class button extends HtmlElement {
        public button() {};
        public button(@DomProperty("innerText") String innerText) { }
    }

    @JSNativeAPI public static class div extends HtmlElement {
        public static class Fake {
            public String className;
        }
        public Fake fake = new Fake();
        public String foobar;
    }
    @JSNativeAPI public static class section extends HtmlElement { }
    @JSNativeAPI public static class header extends HtmlElement { }

    @FunctionalInterface
    public interface ChangeHandler {
        void handle(String value);
    }
    @JSNativeAPI public static class input extends HtmlElement {
        public boolean autofocus = false;
        public enum Autocomplete {OFF, ON}
        public Autocomplete autocomplete;
        public enum InputType {CHECKBOX, TEXT}
        public InputType type;
        public String placeholder;
        public String value;
        public ChangeHandler onchange;
    }

    @JSNativeAPI public static class ul extends HtmlElement {}
    @JSNativeAPI public static class li extends HtmlElement {}
    @JSNativeAPI public static class footer extends HtmlElement {}
    @JSNativeAPI public static class span extends HtmlElement {
        public span() {}
        public span(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class strong extends HtmlElement {
        public strong() {}
        public strong(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class label extends HtmlElement {
        public label() { }
        public label(@DomProperty("innerText") String innerText) {}
        public String _for;
    }
    @JSNativeAPI public static class a extends HtmlElement {
        public String href;
        public a() {}
        public a(@DomProperty("innerText") String innerText) {};
    }

    @JSNativeAPI public static class slot extends HtmlElement {
        public slot(Slot.SlotF0 f) {}
        public <T> slot(Slot.SlotF1<T> f, T el) {}
    }
}
