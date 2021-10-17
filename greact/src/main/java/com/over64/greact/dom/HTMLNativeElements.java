package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.greact.model.async;

public class HTMLNativeElements {
    public interface Component<T extends HtmlElement> {
        default void effect(Object expression) {}
    }

    public static class View {
        public static void remount(View view) {}
        public static Caused causedBy(Object... changes) {
            return new Caused();
        }
        public static class Caused {
            public void remount(View... views) { }
        }
    };

    public interface Component0<T extends HtmlElement> extends Component<T> {
        @async Component0<T> mount();
    }
    public interface Component1<T extends HtmlElement, U> extends Component<T> {
        @async Component0<T> mount(U u);
    }

    // FIXME: for remove
    public interface Component2<T extends HtmlElement, U, V>  extends Component {
        @async Component0<T> mount(U u, V v);
    }
    public interface Component3<T extends HtmlElement, A1, A2, A3>  extends Component {
        @async Component0<T> mount(A1 a1, A2 a2, A3 a3);
    }

    public @interface DomProperty {
        String value();
    }

    public interface NativeElementAsComponent<T extends HtmlElement> extends Component0<T> {
        @Override default Component0<T> mount() { return null; }
    }

    @JSNativeAPI public static class h1 extends HtmlElement implements NativeElementAsComponent<h1>{
        public h1() {};
        public h1(@DomProperty("innerText") String innerText) { }
    }
    @JSNativeAPI public static class h2 extends HtmlElement implements NativeElementAsComponent<h2> {
        public h2() {};
        public h2(@DomProperty("innerText") String innerText) { }
    }

    @JSNativeAPI public static class h3 extends HtmlElement implements NativeElementAsComponent<h3> {
        public h3() {};
        public h3(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static class h4 extends HtmlElement implements NativeElementAsComponent<h4> {
        public h4() {};
        public h4(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static class h5 extends HtmlElement implements NativeElementAsComponent<h5> {
        public h5() {};
        public h5(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static  class button extends HtmlElement implements NativeElementAsComponent<button> {
        public MouseEventHandler<button> onclick;
        public MouseEventHandler<button> ondblclick;
        public MouseEventHandler<button> onblur;
        public button() {}
        public button(@DomProperty("innerText") String innerText) { }
    }

    @JSNativeAPI public static class body extends HtmlElement implements NativeElementAsComponent<body> { }
    @JSNativeAPI public static class div extends HtmlElement implements NativeElementAsComponent<div> {
        public MouseEventHandler<div> onclick;
    }
    @JSNativeAPI public static class nav extends HtmlElement implements NativeElementAsComponent<nav> {}
    @JSNativeAPI public static class section extends HtmlElement implements NativeElementAsComponent<section> { }
    @JSNativeAPI public static class header extends HtmlElement implements NativeElementAsComponent<header> { }


    @JSNativeAPI public static class input extends HtmlElement implements NativeElementAsComponent<input> {
        public boolean autofocus = false;
        public enum Autocomplete {OFF, ON}
        public Autocomplete autocomplete;
        public enum InputType {CHECKBOX, TEXT}
        //public InputType type;
        public String type;
        public String placeholder;
        public String value;
        public Boolean checked;
        public ChangeHandler<input> onchange;
        public native void setCustomValidity(String text);
    }

    @JSNativeAPI public static class select extends HtmlElement implements NativeElementAsComponent<select> {
        public String value;
        public ChangeHandler<select> onchange;
    }
    @JSNativeAPI public static class option extends HtmlElement implements NativeElementAsComponent<option> {
        public boolean selected;
        public String value;
        public option(@DomProperty("innerText") String innerText){}
    }

    @JSNativeAPI public static class ul extends HtmlElement implements NativeElementAsComponent<ul> {}
    @JSNativeAPI public static class li extends HtmlElement implements NativeElementAsComponent<li> {}
    @JSNativeAPI public static class footer extends HtmlElement implements NativeElementAsComponent<footer> {}
    @JSNativeAPI public static class span extends HtmlElement implements NativeElementAsComponent<span> {
        public span() {}
        public span(@DomProperty("innerText") String innerText) {}
        public MouseEventHandler<tbody> onclick;
    }
    @JSNativeAPI public static class strong extends HtmlElement implements NativeElementAsComponent<strong> {
        public strong() {}
        public strong(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class label extends HtmlElement implements NativeElementAsComponent<label> {
        public label() { }
        public label(@DomProperty("innerText") String innerText) {}
        public String _for;
    }
    @JSNativeAPI public static class a extends HtmlElement implements NativeElementAsComponent<a> {
        public String href;
        public a() {}
        public MouseEventHandler<a> onmouseover;
        public MouseEventHandler<a> onmouseout;

        public a(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class table extends HtmlElement implements NativeElementAsComponent<table> {
        public table() {}
    }
    @JSNativeAPI public static class thead extends HtmlElement implements NativeElementAsComponent<thead> { }
    @JSNativeAPI public static class tbody extends HtmlElement implements NativeElementAsComponent<tbody> {
        public MouseEventHandler<tbody> onclick;
    }

    @JSNativeAPI public static class td extends HtmlElement implements NativeElementAsComponent<td> {
        public int colSpan;
        public int rowSpan;
        public MouseEventHandler<tr> onclick;
        public td() {}
        public td(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class tr extends HtmlElement implements NativeElementAsComponent<tr> {
        public MouseEventHandler<tr> onclick;
        public tr() {}
        public tr(HtmlElement child) {}
        public tr(td... childs) {}
    }
    @JSNativeAPI public static class many<T extends HtmlElement> extends HtmlElement {
        public many(T... elements){}
    }


    @JSNativeAPI public static final class slot<T extends HtmlElement> extends HtmlElement {
        public slot(Component0<T> comp) {}
        public <U> slot(Component1<T, U> comp, U u) {}
        public <A1, A2> slot(Component2<T, A1, A2> comp, A1 a1, A2 a2) {}
        public <A1, A2, A3> slot(Component3<T, A1, A2, A3> comp, A1 a1, A2 a2, A3 a3) {}
    }
    @JSNativeAPI public static class style extends HtmlElement implements NativeElementAsComponent<style> {
        public static String id(String prefix) { return  prefix; }
        public style(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class cssclass extends HtmlElement{
        public cssclass(String name, String css) {}
        public static String localClass(String name) {return name; }
    }

    @JSNativeAPI public static class cssmedia extends HtmlElement{
        public cssmedia(String selector, cssclass... classes) {}
    }

    @JSNativeAPI public static class img extends HtmlElement implements NativeElementAsComponent<img> {
        public String src;
    }

    @JSNativeAPI public static class textarea extends HtmlElement implements NativeElementAsComponent<textarea> {
    }

}
