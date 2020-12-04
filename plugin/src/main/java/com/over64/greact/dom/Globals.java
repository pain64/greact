package com.over64.greact.dom;

import com.greact.model.JSExpression;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");

    public static Node greactCleanup(Node el, Integer s, int e) {
        if (s == null) return null;

        var before = (Node) JSExpression.of("el.childNodes[s] || null");
        while (s != e) {
            var next = before.nextSibling;
            el.removeChild(before);
            before = next;
            s++;
        }
        return before;
    }
}
