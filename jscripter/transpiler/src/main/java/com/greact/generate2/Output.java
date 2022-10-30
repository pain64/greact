package com.greact.generate2;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class Output {
    static final VarHandle STRING_VALUE_HANDLE;
    static final VarHandle STRING_CODER_HANDLE;
    static {
        try {
            var mhLookup = MethodHandles.privateLookupIn(String.class, MethodHandles.lookup());
            STRING_VALUE_HANDLE = mhLookup.findVarHandle(String.class, "value", byte[].class);
            STRING_CODER_HANDLE = mhLookup.findVarHandle(String.class, "coder", byte.class);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    final OutputStream jsOut;
    final PrintWriter jsDeps;
    final Set<String> dependencies = new HashSet<>();

    private int deep = 0;
    private boolean newLine = false;

    public Output(OutputStream jsOut, PrintWriter jsDeps) {
        this.jsOut = jsOut;
        this.jsDeps = jsDeps;
    }

    public void deepIn() { deep += 2; newLine = true; }
    public void deepOut() { deep -= 2; newLine = true; }

    public void write(byte[] bytes, int off, int len) {
        try {
            writeSpacesIfNewLine();
            jsOut.write(bytes, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    public void write(String code) {
        var codeBytes = (byte[]) STRING_VALUE_HANDLE.get(code);

        if ((byte) STRING_CODER_HANDLE.get(code) == 1)
            encodeAndWriteUTF8_UTF16(codeBytes);
        else write(codeBytes);
    }

    public void write(Name name) {
        write(name.getByteArray(), name.getByteOffset(), name.getByteLength());
    }

    public void replaceSymbolAndWrite(Name name, int target, int replacement) {
        try {
            writeSpacesIfNewLine();

            for (var i = 0; i < name.getByteLength(); i++) {
                var byte_ = name.getByteArray()[name.getByteOffset() + i];
                if (byte_ == target) jsOut.write(replacement);
                else jsOut.write(byte_);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeRightName(Symbol symbol) {
        if (symbol == null) return;
        if (symbol.owner == null || symbol.owner.name.getByteLength() == 0) {
            write(symbol.name);
            return;
        }

        if (symbol.owner.getKind().isClass()) {
            writeRightName(symbol.owner);
            write(".");
            write(symbol.name);
        } else {
            writeRightName(symbol.owner);
            write("_");
            write(symbol.name);
        }
    }

    private void encodeAndWriteUTF8_UTF16(byte[] val) {
        try {
            writeSpacesIfNewLine();

            var sp = 0;
            var sl = val.length >> 1;

            for (; sp < sl; ++sp) {
                var c = getCharFromByteArray(val, sp);
                if (c >= 128) {
                    break;
                }
                jsOut.write(c);
            }

            while (sp < sl) {
                var c = getCharFromByteArray(val, sp++);
                if (c < 128) {
                    jsOut.write(c);
                } else if (c < 2048) {
                    jsOut.write((byte) (192 | c >> 6));
                    jsOut.write((byte) (128 | c & 63));
                } else if (Character.isSurrogate(c)) {
                    var uc = -1;
                    char c2;
                    if (Character.isHighSurrogate(c) && sp < sl && Character.isLowSurrogate(c2 = getCharFromByteArray(val, sp))) {
                        uc = Character.toCodePoint(c, c2);
                    }
                    jsOut.write((byte) (240 | uc >> 18));
                    jsOut.write((byte) (128 | uc >> 12 & 63));
                    jsOut.write((byte) (128 | uc >> 6 & 63));
                    jsOut.write((byte) (128 | uc & 63));
                    ++sp;
                } else {
                    jsOut.write((byte) (224 | c >> 12));
                    jsOut.write((byte) (128 | c >> 6 & 63));
                    jsOut.write((byte) (128 | c & 63));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private char getCharFromByteArray(byte[] val, int index) {
        index <<= 1;
        return (char) ((val[index++] & 255) | (val[index] & 255) << 8);
    }

    private void writeSpacesIfNewLine() {
        try {
            if (newLine) {
                for (var i = 0; i < deep; i++)
                    jsOut.write(' ');
                newLine = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLn(String code) {
        try {
            write(code);
            jsOut.write('\n');
            newLine = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeNL() {
        try {
            jsOut.write('\n');
            newLine = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
