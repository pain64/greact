package greact.sample.plainjs;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HtmlElement;

//FIXME: this class must be inner class of _06SlotBareNoArgs
class _06Conditional <T extends HtmlElement> implements Component0<T> {
    final boolean cond;
    Component0<T> doThen = () -> null;
    Component0<T> doElse = () -> null;
    _06Conditional(boolean cond) {this.cond = cond;}

    T call(Component0<T> comp) {
        // FIXME:  make lambda to anon inner class pass in JScripter
        return JSExpression.of("comp instanceof Function ? comp() : comp.mount()");
    }

    @Override
    public T mount() {
        return cond ? call(doThen) : call(doElse);
    }
}
