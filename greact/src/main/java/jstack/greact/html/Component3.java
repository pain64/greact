package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.ErasedInterface;

@ErasedInterface
public interface Component3<T extends HTMLElement, A1, A2, A3> extends Component<T> {
    @Async
    Component0<T> render(A1 a1, A2 a2, A3 a3);
}
