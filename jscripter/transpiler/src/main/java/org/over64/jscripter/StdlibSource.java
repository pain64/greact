package org.over64.jscripter;

import com.greact.model.JSSource;

public class StdlibSource implements JSSource {
    @Override
    public String packageName() {
        return "org.over64.jscripter.std.java.lang";
    }
}
