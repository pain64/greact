package com.greact.generate2;

import com.sun.tools.javac.util.Name;

import javax.imageio.IIOException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class Output {
    final DataOutputStream jsOut;
    final PrintWriter jsDeps;
    final Set<String> dependencies = new HashSet<>();

    private int deep = 0;
    private boolean newLine = false;

    public Output(DataOutputStream jsOut, PrintWriter jsDeps) {
        this.jsOut = jsOut;
        this.jsDeps = jsDeps;
    }

    public void deepIn() { deep += 2; newLine = true; }
    public void deepOut() { deep -= 2; newLine = true; }

    public void write(byte[] bytes) {
        try {
            jsOut.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Write error");
        }
    }

    public void write(String code) {
        try {
            if (newLine) {
                for (var i = 0; i < deep; i++)
                    jsOut.write(' ');
                newLine = false;
            }

            jsOut.write(code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Write error");
        }
    }

    public void write(Name name) {
        try {
            if (newLine) {
                for (var i = 0; i < deep; i++)
                    jsOut.write(' ');
                newLine = false;
            }

            jsOut.write(name.getByteArray(), name.getByteOffset(), name.getByteLength());
        } catch (IOException e) {
            throw new RuntimeException("Write error");
        }
    }

    public void writeLn(String code) {
        try {
            write(code);
            jsOut.write('\n');
            newLine = true;
        } catch (IOException e) {
            throw new RuntimeException("Write error");
        }
    }

    public void writeNL() {
        try {
            jsOut.write('\n');
            newLine = true;
        } catch (IOException e) {
            throw new RuntimeException("Write error");
        }
    }

    public void writeCBOpen(boolean spaceBefore) {
        if (spaceBefore) write(" ");
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
