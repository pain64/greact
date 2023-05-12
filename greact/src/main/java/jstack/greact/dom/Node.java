package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Node {
    public Node parentNode;
    public boolean isConnected;
    public Node nextSibling;
    public Node previousSibling;
    public int childElementCount;

    public native Node appendChild(Node child);
    public native Node insertBefore(Node child, Node reference);
    public native Node removeChild(Node child);
    public native void replaceChildren(/* FIXME add args here */);
    public native Node replaceChild(Node newChild, Node oldChild);
}
