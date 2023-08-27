package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.ErasedInterface;

@ErasedInterface
public interface Component0<T extends HTMLElement> extends Component<T> {
    @Async
    Component0<T> render();
}
