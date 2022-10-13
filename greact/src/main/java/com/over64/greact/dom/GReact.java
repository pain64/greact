package com.over64.greact.dom;

import com.greact.model.ErasedInterface;
import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.Component;

import java.util.function.Consumer;

import static com.over64.greact.dom.Globals.document;

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

    @async public static <T extends HTMLElement> T mmountAwaitView(Component<T> comp, Object... args) {
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
    @ErasedInterface
    @FunctionalInterface public interface AsyncRunnable { @async void run(); }
    @ErasedInterface
    @FunctionalInterface public interface AsyncCallable<T> { @async T call(); }
}
