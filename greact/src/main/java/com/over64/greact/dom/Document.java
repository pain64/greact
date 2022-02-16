package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Document {
    public native HTMLElement getElementById(String id);
    public native <T extends HTMLElement> T createElement(String name);
    public native DocumentFragment createDocumentFragment();
}
