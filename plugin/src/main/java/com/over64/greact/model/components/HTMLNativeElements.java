package com.over64.greact.model.components;

public class HTMLNativeElements {

    public static class h1 extends Element{
        public h1() {};
        public h1(String innerText) {
        }
    }

    public static  class button extends Element{
        public button() {};
        public button(String innerText) { }
    }

    public static class div extends Element { }
    public static class section extends Element { }
    public static class header extends Element { }

    @FunctionalInterface
    public interface ChangeHandler {
        void handle(String value);
    }
    public static class input extends Element {
        public boolean autofocus = false;
        public enum Autocomplete {OFF, ON}
        public Autocomplete autocomplete;
        public enum InputType {CHECKBOX, TEXT}
        public InputType type;
        public String placeholder;
        public String value;
        public ChangeHandler onchange;
    }

    public static class ul extends Element {}
    public static class li extends Element {}
    public static class footer extends Element {}
    public static class span extends Element {
        public span() {}
        public span(String innerText) {}
    }
    public static class strong extends Element {
        public strong() {}
        public strong(String innerText) {}
    }
    public static class label extends Element {
        public label() { }
        public label(String innerText) {}
        public String _for;
    }
    public static class a extends Element {
        public String href;
        public a() {}
        public a(String innerText) {};
    }
}
