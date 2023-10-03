package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Async;
import jstack.greact.html.Component;

import java.util.function.Consumer;;

public class GReact {
    /**
     * Tries to render synchronous if can
     *
     * @return rendered component of T or Promise<T>
     */
    public static <T extends HTMLElement> T mmountAwaitView(
        Component<T> comp, Object... args
    ) {
        JSExpression.of("""
            if (comp instanceof HTMLElement)
                return :1;
            else {
                const rendered = :1 instanceof Function
                    ? :1(...:2) : :1._mount(...:2);

                return rendered instanceof Promise
                    ? rendered.then(r =>
                        this._mmountAwaitView(r, [])
                    )
                
                    : this._mmountAwaitView(rendered, [])
            }""", comp, args
        );
        /*
            unreachable code, this code should be removed by
            JS optimizer by DCE pass
        */
        return JSExpression.of("");
    }

    public static <T extends HTMLElement> void mmount(
        T dest, Component<T> newEl, Object... args
    ) {
        JSExpression.of("""
            const rendered = this._mmountAwaitView(:1, ...:2);
            if (rendered instanceof Promise) {
                const placeholder = dest.appendChild(document.createElement('div'));
                rendered.then(r => { dest.replaceChild(r, placeholder); });
            } else
                dest.appendChild(rendered);
            """, newEl, args
        );
    }

    public static <U extends HTMLElement, T extends HTMLElement> void mmountWith(
        U dest, Component<T> newEl, Consumer<T> before, Object... args
    ) {
        JSExpression.of("""
            const rendered = this._mmountAwaitView(:2, ...:4);
            if (rendered instanceof Promise) {
                const placeholder = :1.appendChild(document.createElement('div'));
                rendered.then(r => { :3(r); :1.replaceChild(r, placeholder); });
            } else {
                :3(rendered);
                :1.appendChild(rendered);
            }
            """, dest, newEl, before, args
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
