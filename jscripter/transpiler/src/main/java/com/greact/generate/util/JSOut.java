package com.greact.generate.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class JSOut {
    final Writer out;
    public final Set<String> dependsOn = new TreeSet<>();

    public JSOut(Writer writer) {
        this.out = writer;
    }

    public void write(int sp, String text) {
        try {
            for (int i = 0; i < sp; i++)
                out.write(' ');
            out.write(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> void mkString(Iterator<T> iter, Consumer<T> fn, String prefix, String delim, String suffix) {
        write(0, prefix);
        while (iter.hasNext()) {
            fn.accept(iter.next());
            if (iter.hasNext())
                write(0, delim);
        }
        write(0, suffix);
    }

    public <T> void mkString(Iterable<T> iterable, Consumer<T> fn, String prefix, String delim, String suffix) {
        mkString(iterable.iterator(), fn, prefix, delim, suffix);
    }
}
