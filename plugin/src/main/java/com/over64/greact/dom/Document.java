package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Document {
    public native HtmlElement getElementById(String id);
    public native <T extends HtmlElement> T createElement(String name);
    public native DocumentFragment createDocumentFragment();
}
