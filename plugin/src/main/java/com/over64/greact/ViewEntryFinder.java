package com.over64.greact;

import com.over64.greact.GReactExceptions.NewClassDeniedHere;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import static com.over64.greact.dom.HTMLNativeElements.*;

public class ViewEntryFinder {
    final Symtab symtab;
    final Names names;
    final Types types;
    final Name mountMethodName;

    public ViewEntryFinder(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.symbols = new Symbols();
        this.mountMethodName = names.fromString("mount");
    }

    class Symbols {
        Symbol.ClassSymbol clComponent0 = Util.lookupClass(symtab, names, Component0.class);
        Symbol.ClassSymbol clComponent1 = Util.lookupClass(symtab, names, Component1.class);
        Symbol.ClassSymbol clNativeElementAsComponent = Util.lookupClass(symtab, names, NativeElementAsComponent.class);
    }
    final Symbols symbols;

    public record ViewEntry(JCTree holder, JCTree.JCNewClass view) {}
    enum ComponentKind {NOT_COMPONENT, NATIVE, COMPONENT0, COMPONENT1}

    ComponentKind isTypeImplementsComponent(Type type) {
        Type realType = type;
        if (type.tsym.isAnonymous())
            if (type.tsym instanceof Symbol.ClassSymbol classSym)
                realType = classSym.getSuperclass();

        var ifaces = types.interfaces(realType);
        if (ifaces.stream().anyMatch(iface -> iface.tsym == symbols.clNativeElementAsComponent)) return ComponentKind.NATIVE;
        if (ifaces.stream().anyMatch(iface -> iface.tsym == symbols.clComponent0)) return ComponentKind.COMPONENT0;
        if (ifaces.stream().anyMatch(iface -> iface.tsym == symbols.clComponent1)) return ComponentKind.COMPONENT1;

        return ComponentKind.NOT_COMPONENT;
    }

    public java.util.List<ViewEntry> find(JCTree.JCCompilationUnit cu) {
        var found = new java.util.ArrayList<ViewEntry>();

        cu.accept(new TreeScanner() {
            @Override public void visitMethodDef(JCTree.JCMethodDecl mt) {
                super.visitMethodDef(mt);

                if (mt.name == mountMethodName)
                    if (isTypeImplementsComponent(mt.sym.owner.type) != ComponentKind.NOT_COMPONENT)
                        if (mt.body.stats.last() instanceof JCTree.JCReturn ret)
                            if (ret.expr instanceof JCTree.JCNewClass newClass)
                                found.add(new ViewEntry(ret, newClass));
            }

            @Override public void visitLambda(JCTree.JCLambda lmb) {
                super.visitLambda(lmb);

                if (lmb.type.tsym == symbols.clComponent0 || lmb.type.tsym == symbols.clComponent1)
                    if (lmb.body instanceof JCTree.JCExpression expr) {
                        if (expr instanceof JCTree.JCNewClass newClass)
                            found.add(new ViewEntry(lmb, newClass));
                    } else if (lmb.body instanceof JCTree.JCBlock block)
                        if (block.stats.last() instanceof JCTree.JCReturn ret)
                            if (ret.expr instanceof JCTree.JCNewClass newClass)
                                found.add(new ViewEntry(lmb, newClass));
            }
        });

        cu.accept(new TreeScanner() {
            @Override public void visitNewClass(JCTree.JCNewClass newClass) {
                if (isTypeImplementsComponent(newClass.type) != ComponentKind.NOT_COMPONENT)
                    if (found.stream().noneMatch(ve -> ve.view == newClass))
                        throw new NewClassDeniedHere();
            }
            /*
             * FIXME: нужны более точные проверки
             */
        });

        return found;
    }
}
