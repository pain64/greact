package com.over64.greact.jsx;

import java.util.List;

public interface Ast {
    interface Node extends Ast {}
    interface AttrValue {}

    record Root(List<Node> nodes) implements Ast { }

    record Comment(String value) implements Node {}
    record Text(String value) implements Node {}
    record AttrString(String value) implements AttrValue {}
    record Template(String value) implements AttrValue, Node {}
    record Attr(String name, AttrValue value) implements Ast {}
    record Element(String name, List<Attr> attributes, List<Node> children) implements Node {}
}
