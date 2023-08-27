package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.ErasedInterface;

@ErasedInterface
public interface Component1<T extends HTMLElement, U> extends Component<T> {
    @Async
    Component0<T> render(U u);
}
