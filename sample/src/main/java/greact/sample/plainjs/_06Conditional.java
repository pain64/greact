package greact.sample.plainjs;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

//FIXME: this class must be inner class of _06SlotBareNoArgs
class _06Conditional <T extends HtmlElement> implements Component<T> {
    boolean cond = false;
    Component<T> doThen = () -> null;
    Component<T> doElse = () -> null;

    T call(Component<T> comp) {
        // FIXME:  make lambda to anon inner class pass in JScripter
        return JSExpression.of("comp instanceof Function ? comp() : comp.mount()");
    }

    @Override
    public T mount() {
        return cond ? call(doThen) : call(doElse);
    }
}
