package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.DoNotTranspile;
import jstack.jscripter.transpiler.model.ErasedInterface;

@ErasedInterface
public interface HTMLElementAsComponent<T extends HTMLElement> extends Component0<T> {
    @DoNotTranspile
    @Override default Component0<T> mount() { return null; }
}
