package com.over64.greact;

import com.over64.greact.dom.HTMLNativeElements;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.Objects;
import java.util.Optional;

public class Util {
    final Symtab symtab;
    final Names names;
    final Types types;
    final Symbols symbols;

    public Util(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.symbols = new Symbols();
    }

    class Symbols {
        Symbol.ClassSymbol clComponent0 = lookupClass(HTMLNativeElements.Component0.class);
        Symbol.ClassSymbol clNativeElementAsComponent = lookupClass(HTMLNativeElements.NativeElementAsComponent.class);
        Symbol.ClassSymbol clSlot = lookupClass(HTMLNativeElements.slot.class);
    }

    public Symbol.ClassSymbol lookupClass(Class<?> klass) {
        return symtab.enterClass(symtab.unnamedModule, names.fromString(klass.getName()));
    }

    public <T extends Symbol> Optional<T> lookupMemberOpt(Symbol.ClassSymbol from, String name) {
        @SuppressWarnings("unchecked")
        var res = (Optional<T>) from.getEnclosedElements().stream()
            .filter(el -> el.name.equals(names.fromString(name)))
            .findFirst();
        return res;
    }

    public <T extends Symbol> T lookupMember(Symbol.ClassSymbol from, String name) {
        return this.<T>lookupMemberOpt(from, name).orElseThrow(() ->
            new RuntimeException("No such member with name " + name + " at type " + from));
    }

    /** Gradle 7.2 cannot analyze sealed classes for incremental recompilation */
    public /* sealed */ interface ViewKind {}
    public record IsNotComponent() implements ViewKind {}
    public /* sealed */ interface IsComponent extends ViewKind {
        Type.ClassType htmlElementType();
    }
    public record IsNativeComponent(Type.ClassType htmlElementType) implements IsComponent {}
    /** Gradle 7.2 cannot analyze sealed classes for incremental recompilation */
    public /* sealed */ interface IsCustomComponent extends IsComponent {}
    public record IsSlot(Type.ClassType htmlElementType) implements IsCustomComponent {}
    public record IsComponent0(Type.ClassType htmlElementType) implements IsCustomComponent {}

    ViewKind classifyView(Type type) {
        Type realType = type;
        if (type.tsym.isAnonymous())
            if (type.tsym instanceof Symbol.ClassSymbol classSym)
                realType = classSym.getSuperclass();

        if (realType.tsym == symbols.clSlot)
            return new IsSlot((Type.ClassType) realType.allparams().get(0));

        var ifaces = types.interfaces(realType);
        return ifaces.stream()
            .map(iface -> {
                var params = iface.allparams();
                if (iface.tsym == symbols.clNativeElementAsComponent)
                    return new IsNativeComponent((Type.ClassType) params.get(0));
                else if (iface.tsym == symbols.clComponent0)
                    return new IsComponent0((Type.ClassType) params.get(0));
                else return (ViewKind) null;
            })
            .filter(Objects::nonNull)
            .findFirst().orElse(new IsNotComponent());
    }
}