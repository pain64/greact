package com.over64.greact.model.components;

public class Element implements Mountable {
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
}
