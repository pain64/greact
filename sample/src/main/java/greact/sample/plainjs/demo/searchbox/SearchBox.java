package greact.sample.plainjs.demo.searchbox;

import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.Grid;

public class SearchBox<T> implements Grid.Searcher<T> {

    @FunctionalInterface public interface Provider1<U1, T> {
        @async T[] provide(U1 u1);
    }
    @FunctionalInterface interface AsyncSupplier<T> {
        @async T supply();
    }

    final Control[] controls;
    final AsyncSupplier<T[]> loader;
    boolean canSearch = false;

    void onReadyChanged() {
        canSearch = true;
        for (var control : controls)
            if (!control.ready) {
                canSearch = false;
                break;
            }
        effect(canSearch);
    }

    public <U1> SearchBox(Control<U1> in1, Provider1<U1, T> query) {
        this.controls = new Control[]{in1};
        in1.onReadyChanged = this::onReadyChanged;
        this.loader = () -> query.provide(in1.value);
    }

    @Override public div mount(Grid.DataProvider<T> provider) {
        return new div() {{
            for (var control : controls)
                new slot<div>(control);
            if(canSearch)
                new button("Искать") {{
                    onclick = ev -> provider.onData(loader.supply());
                }};
        }};
    }
}
