package com.over64.greact;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Names;

public class Util {
    public static Symbol.ClassSymbol lookupClass(Symtab symtab, Names names, Class<?> klass) {
        return symtab.enterClass(symtab.unnamedModule, names.fromString(klass.getName()));
    }

    public static <T extends Symbol> T lookupMember(Names names, Symbol.ClassSymbol from, String name) {
        @SuppressWarnings("unchecked")
        var res = (T) from.getEnclosedElements().stream()
            .filter(el -> el.name.equals(names.fromString(name)))
            .findFirst().orElseThrow(() ->
                new RuntimeException("No such member with name " + name + " at type " + from));
        return res;
    }
}
