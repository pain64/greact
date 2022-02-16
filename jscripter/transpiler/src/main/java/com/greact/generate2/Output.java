package com.greact.generate2;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class Output {
    final PrintWriter jsOut;
    final PrintWriter jsDeps;
    final Set<String> dependencies = new HashSet<>();

    private int deep = 0;
    private boolean newLine = false;

    public Output(PrintWriter jsOut, PrintWriter jsDeps) {
        this.jsOut = jsOut;
        this.jsDeps = jsDeps;
    }

    public void deepIn() { deep += 2; newLine = true; }
    public void deepOut() { deep -= 2; newLine = true; }

    public void write(String code) {
        if (newLine) {
            for (var i = 0; i < deep; i++)
                jsOut.print(' ');
            newLine = false;
        }

        jsOut.write(code);
    }

    public void writeLn(String code) {
        write(code);
        jsOut.print("\n");
        newLine = true;
    }

    public void writeNL() {
        jsOut.print("\n");
        newLine = true;
    }

    public void writeCBOpen(boolean spaceBefore) {
        if(spaceBefore) write(" ");
        writeLn("{"); deep += 2;
    }
    public void writeCBEnd(boolean newLine) {
        deep -= 2;
        if (newLine) writeLn("}");
        else write("}");
    }

    public void addDependency(String dep) {
        if (dependencies.add(dep)) jsDeps.println(dep);
    }

    public <T> void mkString(Iterator<T> iter, Consumer<T> fn, String prefix, String delim, String suffix) {
        write(prefix);
        while (iter.hasNext()) {
            fn.accept(iter.next());
            if (iter.hasNext()) write(delim);
        }
        write(suffix);
    }

    public <T> void mkString(Iterable<T> iterable, Consumer<T> fn, String prefix, String delim, String suffix) {
        mkString(iterable.iterator(), fn, prefix, delim, suffix);
    }
}
