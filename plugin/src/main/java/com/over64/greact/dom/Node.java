package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Node {
    public Node nextSibling;

    public native Node appendChild(Node child);
    public native Node removeChild(Node child);
}
