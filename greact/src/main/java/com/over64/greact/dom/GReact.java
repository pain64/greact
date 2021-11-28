package com.over64.greact.dom;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.Component;

import java.util.function.Consumer;

import static com.over64.greact.dom.Globals.document;

public class GReact {
    public static HtmlElement element;
    public static <T extends HtmlElement> T entry(java.lang.Runnable maker) {
        maker.run();
        return null;
    }

    public static <T extends HtmlElement> T mount(T dest, Component<T> newEl, Object... args) {
        element = dest;
        JSExpression.of("newEl instanceof Function ? newEl(...args) : newEl.mount(...args)");
        return null;
    }

    @async static <T extends HtmlElement> T mmountAwaitView(Component<T> comp, Object... args) {
        return JSExpression.of("""
            comp instanceof HTMLElement ? comp :
                comp instanceof Function ? await this.mmountAwaitView(await comp(...args), []) :
                    await this.mmountAwaitView(await comp.mount(...args), [])
            """);

    }

    @async public static <T extends HtmlElement> void mmount(T dest, Component<T> newEl, Object... args) {
        var view = JSExpression.<HtmlElement>of("await this.mmountAwaitView(newEl, args)");
        dest.appendChild(view);
    }


    public static <U extends HtmlElement> void make(HtmlElement parent, String name, Consumer<U> maker) {
        U el = document.createElement(name);
        maker.accept(el);
        parent.appendChild(el);
    }

    public static <T extends HtmlElement> T mountMe(String htmlElName) {
        T newEl = document.createElement(htmlElName);
        element.appendChild(newEl);
        return newEl;
    }

    @FunctionalInterface public interface AsyncRunnable { @async void run(); }
    @FunctionalInterface public interface AsyncCallable<T> { @async T call(); }
}
