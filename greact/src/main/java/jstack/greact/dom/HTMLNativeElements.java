package jstack.greact.dom;

import jstack.jscripter.transpiler.model.DoNotTranspile;
import jstack.jscripter.transpiler.model.ErasedInterface;
import jstack.jscripter.transpiler.model.JSNativeAPI;
import jstack.jscripter.transpiler.model.async;

public class HTMLNativeElements {
    @ErasedInterface
    public interface Component<T extends HTMLElement> {
        @DoNotTranspile
        default void effect(Object expression) {}
    }

    @ErasedInterface
    public interface Component0<T extends HTMLElement> extends Component<T> {
        @async Component0<T> mount();
    }
    @ErasedInterface
    public interface Component1<T extends HTMLElement, U> extends Component<T> {
        @async Component0<T> mount(U u);
    }

    // FIXME: for remove
    @ErasedInterface
    public interface Component2<T extends HTMLElement, U, V>  extends Component<T> {
        @async Component0<T> mount(U u, V v);
    }
    @ErasedInterface
    public interface Component3<T extends HTMLElement, A1, A2, A3>  extends Component<T> {
        @async Component0<T> mount(A1 a1, A2 a2, A3 a3);
    }

    @ErasedInterface
    public @interface DomProperty {
        String value();
    }

    @ErasedInterface
    public interface NativeElementAsComponent<T extends HTMLElement> extends Component0<T> {
        @DoNotTranspile
        @Override default Component0<T> mount() { return null; }
    }

    @JSNativeAPI public static class h1 extends HTMLElement implements NativeElementAsComponent<h1>{
        public h1() {};
        public h1(@DomProperty("innerText") String innerText) { }
    }
    @JSNativeAPI public static class h2 extends HTMLElement implements NativeElementAsComponent<h2> {
        public h2() {};
        public h2(@DomProperty("innerText") String innerText) { }
    }

    @JSNativeAPI public static class h3 extends HTMLElement implements NativeElementAsComponent<h3> {
        public h3() {};
        public h3(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static class h4 extends HTMLElement implements NativeElementAsComponent<h4> {
        public h4() {};
        public h4(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static class h5 extends HTMLElement implements NativeElementAsComponent<h5> {
        public h5() {};
        public h5(@DomProperty("innerText") String innerText) {
        }
    }

    @JSNativeAPI public static class i extends HTMLElement implements NativeElementAsComponent<i>{
        public String dataFeather;
        public i() {};
    }

    @JSNativeAPI public static class canvas extends HTMLElement implements NativeElementAsComponent<canvas> {
        public  String width;
        public  String height;
        public canvas() {};
    }

    @JSNativeAPI public static class pre extends HTMLElement implements NativeElementAsComponent<pre> {
    }

    @JSNativeAPI public static class code extends HTMLElement implements NativeElementAsComponent<code> {
    }

    @JSNativeAPI public static class form extends HTMLElement implements NativeElementAsComponent<form> {
        public String action;
        public String name;
        public String method;
        public String acceptCharset;
    }

    @JSNativeAPI public static  class button extends HTMLElement implements NativeElementAsComponent<button> {
        public String type;
        public button() {}
        public button(@DomProperty("innerText") String innerText) { }
    }

    @JSNativeAPI public static class body extends HTMLElement implements NativeElementAsComponent<body> { }
    @JSNativeAPI public static class div extends HTMLElement implements NativeElementAsComponent<div> {
    }
    @JSNativeAPI public static class nav extends HTMLElement implements NativeElementAsComponent<nav> {}
    @JSNativeAPI public static class section extends HTMLElement implements NativeElementAsComponent<section> { }
    @JSNativeAPI public static class header extends HTMLElement implements NativeElementAsComponent<header> { }


    @JSNativeAPI public static class input extends HTMLElement implements NativeElementAsComponent<input> {
        public boolean autofocus = false;
        public boolean required;
        public boolean readOnly;
        public enum Autocomplete {OFF, ON}
        public Autocomplete autocomplete;
        public enum InputType {CHECKBOX, TEXT}
        //public InputType type;
        public String name;
        public String type;
        public String minLength;
        public String maxLength;
        public String placeholder;
        public String value;
        public long valueAsNumber;
        public Boolean checked;
        public native void setCustomValidity(String text);
    }

    @JSNativeAPI public static class select extends HTMLElement implements NativeElementAsComponent<select> {
        public String name;
        public String value;
    }
    @JSNativeAPI public static class option extends HTMLElement implements NativeElementAsComponent<option> {
        public boolean selected;
        public String value;
        public option(@DomProperty("innerText") String innerText){}
    }

    @JSNativeAPI public static class ul extends HTMLElement implements NativeElementAsComponent<ul> {}
    @JSNativeAPI public static class li extends HTMLElement implements NativeElementAsComponent<li> {}
    @JSNativeAPI public static class footer extends HTMLElement implements NativeElementAsComponent<footer> {}
    @JSNativeAPI public static class span extends HTMLElement implements NativeElementAsComponent<span> {
        public span() {}
        public span(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class p extends HTMLElement implements NativeElementAsComponent<p> {
        public p() {}
        public p(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class strong extends HTMLElement implements NativeElementAsComponent<strong> {
        public strong() {}
        public strong(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class label extends HTMLElement implements NativeElementAsComponent<label> {
        public label() { }
        public label(@DomProperty("innerText") String innerText) {}
        public String _for;
    }
    @JSNativeAPI public static class a extends HTMLElement implements NativeElementAsComponent<a> {
        public String href;
        public a() {}
        public a(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class table extends HTMLElement implements NativeElementAsComponent<table> {
        public table() {}
    }
    @JSNativeAPI public static class thead extends HTMLElement implements NativeElementAsComponent<thead> { }
    @JSNativeAPI public static class tbody extends HTMLElement implements NativeElementAsComponent<tbody> { }

    @JSNativeAPI public static class td extends HTMLElement implements NativeElementAsComponent<td> {
        public int colSpan;
        public int rowSpan;
        public td() {}
        public td(@DomProperty("innerText") String innerText) {}
    }

    @JSNativeAPI public static class tr extends HTMLElement implements NativeElementAsComponent<tr> {
        public tr() {}
        public tr(HTMLElement child) {}
        public tr(td... childs) {}
    }
    @JSNativeAPI public static class many<T extends HTMLElement> extends HTMLElement {
        public many(T... elements){}
    }


    @JSNativeAPI public static final class slot<T extends HTMLElement> extends HTMLElement implements NativeElementAsComponent<T> {
        public slot(Component0<T> comp) {}
        public <U> slot(Component1<T, U> comp, U u) {}
        public <A1, A2> slot(Component2<T, A1, A2> comp, A1 a1, A2 a2) {}
        public <A1, A2, A3> slot(Component3<T, A1, A2, A3> comp, A1 a1, A2 a2, A3 a3) {}
    }
    @JSNativeAPI public static class style extends HTMLElement implements NativeElementAsComponent<style> {
        public static String id(String prefix) { return  prefix; }
        public style(@DomProperty("innerText") String innerText) {}
    }
    @JSNativeAPI public static class cssclass extends HTMLElement {
        public cssclass(String name, String css) {}
        public static String localClass(String name) {return name; }
    }

    @JSNativeAPI public static class cssmedia extends HTMLElement {
        public cssmedia(String selector, cssclass... classes) {}
    }

    @JSNativeAPI public static class img extends HTMLElement implements NativeElementAsComponent<img> {
        public String src;
        public String align;
    }

    @JSNativeAPI public static class textarea extends HTMLElement implements NativeElementAsComponent<textarea> {
    }

    @JSNativeAPI public static class iframe extends HTMLElement implements NativeElementAsComponent<iframe> {
        public String sandbox;
        public String src;
    }
    @JSNativeAPI public static class html extends HTMLElement implements NativeElementAsComponent<html> {
    }
    @JSNativeAPI public static class br extends HTMLElement implements NativeElementAsComponent<html> {
    }
}