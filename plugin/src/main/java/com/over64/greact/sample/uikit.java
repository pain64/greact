package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;
import com.over64.greact.model.components.Slot;

import java.util.Arrays;
import java.util.stream.IntStream;

public class uikit {
    public static class pagination<T> implements Component {
        private final T[] data;
        private int page = 0;

        int by = 10;
        Slot.SlotF1<T> item = (t) -> { };

        public pagination(T[] data) {
            this.data = data;
        }

        T[] currentData() {
            // use js Array.slice(page * by, page * (by + 1))
            return (T[]) Arrays.stream(data).skip(page * by).limit(by).toArray();
        }

        @Override
        public void mount(HtmlElement dom) {
            // var pages = Array.from<Int>();
            // for(var i = 0; i < data.length / by; i++) pages.push(i);
            var pages = IntStream.range(0, data.length / by).toArray();

            GReact.mount(dom, new div() {{
                for (var el : currentData())
                    new slot(item, el);

                new div() {{
                    className = "pages-holder";
                    for (var p : pages)
                        new span(String.valueOf(p + 1)) {{
                            if (p == page) style.color = "green";
                            onclick = () -> GReact.effect(page = p);
                        }};
                }};

            }});
        }
    }
}
