package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.DoNotTranspile;
import jstack.jscripter.transpiler.model.ErasedInterface;

@ErasedInterface
public interface Component<T extends HTMLElement> {
    @DoNotTranspile
    default void effect(Object expression) { }
}
