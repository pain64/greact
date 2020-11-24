package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.over64.greact.model.components.Mountable;

@JSNativeAPI
public class HtmlElement extends Node implements Mountable {
    public static class Style {
        public String color;
    }

    // HTML Global Attributes
    public String id;
    public String className;
    public String innerText;
    public String lang;
    public Style style = new Style();

    // HTML Event Attributes
    //   Mouse events
    public interface MouseEventHandler {
        void handle();
    }

    public MouseEventHandler onclick;
    public MouseEventHandler ondblclick;
    public MouseEventHandler onblur;

    public enum Key {UP, ENTER, ESC}

    public interface KeyHandler {
        void handle(Key key);
    }

    public KeyHandler onkeyup;
}