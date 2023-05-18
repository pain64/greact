package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public final class slot<T extends HTMLElement> extends HTMLElement implements HTMLElementAsComponent<T> {
    public slot(Component0<T> comp) { }
    public <U> slot(Component1<T, U> comp, U u) { }
    public <A1, A2> slot(Component2<T, A1, A2> comp, A1 a1, A2 a2) { }
    public <A1, A2, A3> slot(Component3<T, A1, A2, A3> comp, A1 a1, A2 a2, A3 a3) { }
}
