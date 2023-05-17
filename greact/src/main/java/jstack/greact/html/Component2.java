package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.ErasedInterface;

// FIXME: for remove
@ErasedInterface
public interface Component2<T extends HTMLElement, U, V> extends Component<T> {
    @Async
    Component0<T> mount(U u, V v);
}
