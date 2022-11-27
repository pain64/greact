package jstack.jscripter.transpiler;

import jstack.jscripter.transpiler.model.JSSource;

public class StdlibSource implements JSSource {
    @Override
    public String packageName() {
        return "jstack.jscripter.std.java.lang";
    }
}
