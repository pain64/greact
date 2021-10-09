package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Node {
    public Node nextSibling;
    public Node previousSibling;
    public int childElementCount;

    public native Node appendChild(Node child);
    public native Node insertBefore(Node child, Node reference);
    public native Node removeChild(Node child);
    public native void replaceChildren(/* FIXME add args here */);
}
