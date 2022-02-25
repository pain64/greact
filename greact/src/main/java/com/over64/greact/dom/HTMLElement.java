package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.greact.model.async;

@JSNativeAPI public class HTMLElement extends Node {
    public static class Style {
        public String color;
        public String backgroundColor;
        public String border;
        public String borderLeft;
        public String borderRight;
        public String borderTop;
        public String borderBottom;
        public String borderRadius;
        public String borderColor;
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

    public MouseEventHandler onclick;
    public MouseEventHandler ondblclick;
    public MouseEventHandler onblur;
    public ChangeHandler onchange;
    public MouseEventHandler onmouseout;
    public MouseEventHandler onmouseover;



    // HTML Event Attributes
    public static class Event {
        public native void stopPropagation();
        public HTMLElement target;
    }

    @FunctionalInterface public interface ChangeHandler {
        @async void handle(Event ev);
    }

    @FunctionalInterface public interface MouseEventHandler {
        @async void handle(Event ev);
    }

    public static class KeyEvent extends Event { }

    public interface KeyHandler {
        void handle(KeyEvent event);
    }

    public KeyHandler onkeyup;
}