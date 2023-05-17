package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Async;
import jstack.greact.html.Component;

import java.util.function.Consumer;

import static jstack.greact.dom.Globals.document;

public class GReact {
    public static HTMLElement element;
    public static <T extends HTMLElement> T entry(java.lang.Runnable maker) {
        maker.run();
        return null;
    }

    public static <T extends HTMLElement> T mount(T dest, Component<T> newEl, Object... args) {
        element = dest;
        JSExpression.of("newEl instanceof Function ? newEl(...args) : newEl._mount(...args)");
        return null;
    }

    @Async public static <T extends HTMLElement> T mmountAwaitView(Component<T> comp, Object... args) {
        return JSExpression.ofAsync("""
            comp instanceof HTMLElement ? comp :
                comp instanceof Function ? await this._mmountAwaitView(await comp(...args), []) :
                    await this._mmountAwaitView(await comp._mount(...args), [])
            """);

    }

    public static <T extends HTMLElement> void mmount(T dest, Component<T> newEl, Object... args) {
        var placeholder = dest.appendChild(document.createElement("div"));
        JSExpression.<HTMLElement>of("this._mmountAwaitView(newEl, ...args).then(v => dest.replaceChild(v, placeholder))");
    }

    public static <U extends HTMLElement, T extends HTMLElement> void mmountWith(U dest, Component<T> newEl, Consumer<T> before, Object... args) {
        var placeholder = dest.appendChild(document.createElement("div"));
        JSExpression.<HTMLElement>of("this._mmountAwaitView(newEl, ...args).then(v => { before(v); dest.replaceChild(v, placeholder); })");
    }


    public static <U extends HTMLElement> void make(HTMLElement parent, String name, Consumer<U> maker) {
        U el = document.createElement(name);
        maker.accept(el);
        parent.appendChild(el);
    }

    public static <T extends HTMLElement> T mountMe(String htmlElName) {
        T newEl = document.createElement(htmlElName);
        element.appendChild(newEl);
        return newEl;
    }

    public static <T extends HTMLElement> T replace(T el, HTMLElement holder) {
        if(holder != null)
            holder.parentNode.replaceChild(el, holder);

        return el;
    }

    @FunctionalInterface public interface AsyncRunnable { @Async void run(); }
    @FunctionalInterface public interface AsyncCallable<T> { @Async T call(); }
}
