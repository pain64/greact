package com.greact.generate.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;

public class JavaStdShim {
    final Types types;
    final Map<Name, Type> shimConversions;

    public JavaStdShim(Types types, Map<Name, Type> shimConversions) {
        this.types = types;
        this.shimConversions = shimConversions;
    }

    public Type findShimmedType(Type javaStdType) {
        return shimConversions.get(((Symbol.ClassSymbol) javaStdType.tsym).fullname);
    }

    public Symbol.MethodSymbol findShimmedMethod(Type shimmedType, Symbol.MethodSymbol method) {
        return (Symbol.MethodSymbol) shimmedType.tsym.getEnclosedElements().stream()
            .filter(el -> el instanceof ExecutableElement)
            .filter(el -> el.name.equals(method.name))
            .filter(el -> types.isSameType(method.type, el.type))
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("bad shimmed type " + shimmedType));
    }
}
