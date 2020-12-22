package com.over64.greact.dom;

public class Fragment {
    @FunctionalInterface
    public interface ViewFragment {
        void apply();
    }
    @FunctionalInterface
    public interface Renderer {
        void render();
    }

    public final Renderer renderer;
    final Node dest;
    Node before = null;
    Node last = null;
    int count = 0;

    public Fragment(Renderer renderer, Node dest) {
        this.renderer = renderer;
        this.dest = dest;
    }

    public static Fragment of(Renderer renderer, Node dest) {
        return new Fragment(renderer, dest);
    }

    public void cleanup() {
        while (count > 0) {
            var prev = last.previousSibling;
            before = last.nextSibling;
            dest.removeChild(last);
            last = prev;
            count--;
        }
    }

    public void appendChild(Node el) {
        dest.insertBefore(el, before);
        last = el;
        count++;
    }
}
