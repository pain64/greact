package com.over64.greact.model.components;

import org.over64.jscripter.std.js.Node;

public class Element extends Node implements Mountable {
    public static class Style {
        public String color;
    }

    // HTML Global Attributes
    public String id;
    public String className;
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
