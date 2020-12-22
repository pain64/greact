package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

public class HTMLNativeElements {
    public interface Component {
        default void effect(Object expression) {}
    }
    public interface Component0<T extends HtmlElement> extends Component {
        T mount();
    }
    public interface Component1<T extends HtmlElement, U> extends Component {
        T mount(U u);
    }
    public interface Component2<T extends HtmlElement, U, V>  extends Component {
        T mount(U u, V v);
    }

    public @interface DomProperty {
        String value();
    }

    @JSNativeAPI public static class h1 extends HtmlElement {
        public h1() {};
        public h1(@DomProperty("innerText") String innerText) {
        }
    }
    @JSNativeAPI public static class h2 extends HtmlElement {
        public h2() {};
        public h2(@DomProperty("innerText") String innerText) {
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

        public div(HtmlElement child) {};
        public div(Component0... components) {};
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
        public a(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class table extends HtmlElement {
        public table() {}
    }

    @JSNativeAPI public static class td extends HtmlElement {
        public td() {}
        public td(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class tr extends HtmlElement {
        public tr() {}
        public tr(HtmlElement child) {}
        public tr(td... childs) {}
    }
    @JSNativeAPI public static class many<T extends HtmlElement> extends HtmlElement {
        public many(T... elements){}
    }


    @JSNativeAPI public static class slot<T extends HtmlElement> extends HtmlElement {
        public slot(Component0<T> comp) {}
        public <U> slot(Component1<T, U> comp, U u) {}
    }
    @JSNativeAPI public static class style extends HtmlElement{
        public static String id(String prefix) { return  prefix; }
        public style(String... parts) {}
    }
    @JSNativeAPI public static class cssclass extends HtmlElement{
        public cssclass(String name, String css) {}
        public static String localClass(String name) {return name; }
    }

    @JSNativeAPI public static class cssmedia extends HtmlElement{
        public cssmedia(String selector, cssclass... classes) {}
    }
}
