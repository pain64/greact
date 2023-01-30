package com.over64.greact;

import com.over64.greact.dom.HTMLNativeElements;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    final Symtab symtab;
    final Names names;
    final Types types;
    final Symbols symbols;
    final Log javacLog;
    final JCDiagnostic.Factory diagnosticsFactory;
    final JavacMessages javacMessages;

    public Util(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.symbols = new Symbols();
        this.javacLog = Log.instance(context);
        this.diagnosticsFactory = JCDiagnostic.Factory.instance(context);
        this.javacMessages = JavacMessages.instance(context);
        var bundle = ResourceBundle.getBundle(
            "com.over64.greact.MessagesBundle_en", new Locale("en", "US")
        );
        javacMessages.add(locale -> bundle);
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

    public List<Symbol.MethodSymbol> lookupMemberAll(Symbol.ClassSymbol from, String name) {
        var res = from.getEnclosedElements().stream()
            .filter(el -> el.name.equals(names.fromString(name)))
            .map(n -> ((Symbol.MethodSymbol) n))
            .toList();
        return res;
    }

    public <T extends Symbol> T lookupMember(Symbol.ClassSymbol from, String name) {
        return this.<T>lookupMemberOpt(from, name).orElseThrow(() ->
            new RuntimeException("No such member with name " + name + " at type " + from));
    }

    /**
     * Gradle 7.2 cannot analyze sealed classes for incremental recompilation
     */
    public /* sealed */ interface ViewKind { }
    public record IsNotComponent() implements ViewKind { }
    public /* sealed */ interface IsComponent extends ViewKind { }
    public record IsNativeComponent(Type.ClassType htmlElementType) implements IsComponent { }
    /**
     * Gradle 7.2 cannot analyze sealed classes for incremental recompilation
     */
    public /* sealed */ interface IsCustomComponent extends IsComponent { }
    public record IsSlot() implements IsCustomComponent { }
    public record IsComponent0() implements IsCustomComponent { }

    ViewKind classifyView(Type type) {
        Type realType = type;
        if (type.tsym.isAnonymous())
            if (type.tsym instanceof Symbol.ClassSymbol classSym)
                realType = classSym.getSuperclass();

        if (realType.tsym == symbols.clSlot)
            return new IsSlot();

        var ttype = type;

        var ifaces = types.interfaces(realType);
        return ifaces.stream()
            .map(iface -> {
                var params = iface.allparams();
                if (iface.tsym == symbols.clNativeElementAsComponent)
                    return new IsNativeComponent((Type.ClassType) params.get(0));
                else if (iface.tsym == symbols.clComponent0)
                    return new IsComponent0();
                else return (ViewKind) null;
            })
            .filter(Objects::nonNull)
            .findFirst().orElseGet(() -> {
                if (ttype.tsym instanceof Symbol.ClassSymbol classSym)
                    if (classSym.getSuperclass() != null)
                        if (classSym.getSuperclass().tsym != null)
                            return classifyView(classSym.getSuperclass());

                return new IsNotComponent();
            });
    }

    public void writeCompilationError(JCTree.JCCompilationUnit cu, JCTree tree, String error) {
        var pos = new JCDiagnostic.SimpleDiagnosticPosition(tree.pos);
        javacLog.useSource(cu.getSourceFile());
        javacLog.error(
            JCDiagnostic.DiagnosticFlag.API,
            pos, diagnosticsFactory.errorKey("ssql", error)
        );
    }

    // FIXME: use writeCompilationError instead
    public String treeSourcePosition(JCTree.JCCompilationUnit cu, JCTree tree) {
        var src = new DiagnosticSource(cu.getSourceFile(), javacLog);
        return "%s:%d:%d".formatted(
            src.getFile().getName(),
            src.getLineNumber(tree.pos),
            src.getColumnNumber(tree.pos, true));
    }
}
