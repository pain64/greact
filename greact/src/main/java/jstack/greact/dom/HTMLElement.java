package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;
import jstack.jscripter.transpiler.model.Async;

@JSNativeAPI public class HTMLElement extends Node {
    public String pattern;
    public static class Style {
        public String color;
        public String lineHeight;
        public String letterSpacing;
        public String textDecoration;
        public String font;
        public String backgroundImage;
        public String backgroundPosition;
        public String borderWidth;
        public String backgroundColor;
        public String background;
        public String opacity;
        public String backgroundSize;
        public String border;
        public String borderLeft;
        public String borderRight;
        public String borderTop;
        public String borderBottom;
        public String borderRadius;
        public String borderColor;
        public String padding;
        public String paddingLeft;
        public String paddingRight;
        public String paddingTop;
        public String paddingBottom;
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
        public String alignSelf;
        public String alignItems;
        public String whiteSpace;
        public String height;
        public String width;
        public String textAlign;
        public String fontSize;
        public String fontWeight;
        public String fontStyle;
        public String fontFamily;
        public String gap;
        public String pageBreakAfter;
        public String backgroundRepeat;
        public String verticalAlign;
    }

    // HTML Global Attributes
    public String id;
    public String name;
    public String className;
    public String innerText;
    public String innerHTML;
    public String lang;
    public String title;
    public Style style = new Style();

    public final DOMTokenList classList = new DOMTokenList();

    public Object dependsOn;

    public MouseEventHandler onclick;
    public MouseEventHandler ondblclick;
    public FocusEventHandler onfocus;
    public FocusEventHandler onblur;
    public ChangeHandler onchange;
    public MouseEventHandler onmouseout;
    public MouseEventHandler onmouseover;
    public MouseEventHandler onmouseenter;
    public MouseEventHandler onmouseleave;

    public native void setAttribute(String name, Object value);
    public native String getAttribute(String name);

    public native boolean contains(HTMLElement element);
    public native boolean matches(String pattern);

    // HTML Event Attributes
    public static class Event {
        public native void stopPropagation();
        public native void preventDefault();
        public HTMLElement target;
    }

    @FunctionalInterface public interface ChangeHandler {
        @Async void handle(Event ev);
    }

    @FunctionalInterface public interface MouseEventHandler {
        @Async void handle(Event ev);
    }

    @FunctionalInterface public interface SubmitEventHandler {
        @Async void handle(SubmitEvent ev);
    }

    @FunctionalInterface public interface FocusEventHandler {
        @Async void handle(FocusEvent ev);
    }

    public static class KeyEvent extends Event { }
    public static class SubmitEvent extends Event { }
    public static class FocusEvent extends Event { }

    public interface KeyHandler {
        void handle(KeyEvent event);
    }

    public KeyHandler onkeyup;
}