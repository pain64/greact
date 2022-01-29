package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.greact.model.async;

@JSNativeAPI public class HTMLElement extends Node {
    public static class Style {
        public String color;
        public String borderColor;
        public String backgroundColor;
        public String border;
        public String borderRadius;
        public String borderBottom;
        public String padding;
        public String margin;
        public String marginBottom;
        public String marginTop;
        public String marginLeft;
        public String marginRight;
        public String maxWidth;
        public String minWidth;
        public String maxHeight;
        public String minHeight;
        public String display;
        public String cursor;
        public String justifyContent;
        public String flexWrap;
        public String flexDirection;
        public String alignItems;
        public String whiteSpace;
        public String height;
        public String width;
        public String textAlign;
        public String fontSize;
        public String fontWeight;
    }

    // HTML Global Attributes
    public String id;
    public String className;
    public String innerText;
    public String innerHTML;
    public String lang;
    public String title;
    public Style style = new Style();

    public Object dependsOn;


    // HTML Event Attributes
    public static class Event<T> {
        public native void stopPropagation();
        public T target;
    }

    @FunctionalInterface
    public interface ChangeHandler<T extends HTMLElement> {
        @async void handle(Event<T> ev);
    }

    @FunctionalInterface
    public interface MouseEventHandler<T extends HTMLElement> {
        @async void handle(Event<T> ev);
    }

    public static class KeyEvent extends Event<HTMLElement> {

    }

    public interface KeyHandler {
        void handle(KeyEvent event);
    }

    public KeyHandler onkeyup;
}