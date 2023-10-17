package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Async;
import jstack.greact.html.Component;

import java.util.function.Consumer;

public class GReact {
    /**
     * Tries to render synchronous if can
     *
     * @return rendered component of T or Promise<T>
     */
    public static <T extends HTMLElement> T renderAwaitView(
        Component<T> comp, Object... args
    ) {
        JSExpression.of("""
            if (comp instanceof HTMLElement)
                return comp;
            else {
                const rendered = comp instanceof Function
                    ? comp(...args) : comp._render(...args);

                return rendered instanceof Promise
                    ? rendered.then(r =>
                        this._renderAwaitView(r, [])
                    )
                
                    : this._renderAwaitView(rendered, [])
            }"""
        );
        /*
            unreachable code, this code should be removed by
            JS optimizer by DCE pass
        */
        return JSExpression.of("");
    }

    public static <T extends HTMLElement> void mount(
        T dest, Component<T> newEl, Object... args
    ) {
        JSExpression.of("""
            const rendered = this._renderAwaitView(newEl, ...args);
            if (rendered instanceof Promise) {
                const placeholder = dest.appendChild(document.createElement('div'));
                rendered.then(r => { dest.replaceChild(r, placeholder); });
            } else
                dest.appendChild(rendered);
            """
        );
    }

    public static <U extends HTMLElement, T extends HTMLElement> void mountWith(
        U dest, Component<T> newEl, Consumer<T> before, Object... args
    ) {
        JSExpression.of("""
            const rendered = this._renderAwaitView(newEl, ...args);
            if (rendered instanceof Promise) {
                const placeholder = dest.appendChild(document.createElement('div'));
                rendered.then(r => { before(r); dest.replaceChild(r, placeholder); });
            } else {
                before(rendered);
                dest.appendChild(rendered);
            }
            """
        );
    }

    public static <T extends HTMLElement> T replace(T el, HTMLElement holder) {
        if (holder != null)
            holder.parentNode.replaceChild(el, holder);

        return el;
    }

    @FunctionalInterface public interface AsyncRunnable {
        @Async void run();
    }
    @FunctionalInterface public interface AsyncCallable<T> {
        @Async T call();
    }
}