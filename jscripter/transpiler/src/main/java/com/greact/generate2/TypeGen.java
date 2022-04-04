package com.greact.generate2;

import com.greact.generate2.lookahead.HasSuperConstructorCall;
import com.greact.model.DoNotTranspile;
import com.greact.model.JSNativeAPI;
import com.greact.model.Require;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeGen extends ClassBodyGen {
    @Override public void visitPackageDef(JCTree.JCPackageDecl __) { }
    @Override public void visitImport(JCTree.JCImport __) { }
    @Override public void visitTopLevel(JCTree.JCCompilationUnit cu) {
        for (var def : cu.defs) def.accept(this);
    }

    @Override public void visitClassDef(JCTree.JCClassDecl classDef) {
        var isInnerEnum = classDef.sym.isEnum() && classDef.sym.owner.getKind().isClass();
        var cssRequire = classDef.sym.getAnnotation(Require.CSS.class);
        if (cssRequire != null)
            for (var dep : cssRequire.value())
                out.addDependency(dep);

        if (classDef.getKind() == Tree.Kind.INTERFACE) return;
        if (classDef.sym.getAnnotation(JSNativeAPI.class) != null) return;

        if (!classDef.type.tsym.isAnonymous() &&
            classDef.sym.owner instanceof Symbol.ClassSymbol) {

            if (!classDef.sym.isStatic())
                throw new RuntimeException("Cannot compile non static inner classes yet");

            out.write("static ");
            out.write(classDef.getSimpleName().toString());
            out.write(" = class");
        } else {
            out.write("class");
            if (!classDef.type.tsym.isAnonymous()) {
                out.write(" ");
                out.write(cu.getPackage().getPackageName().toString().replace(".", "_"));
                out.write("_");
                out.write(classDef.getSimpleName().toString());
            }
        }
        // -----
        var groups = new HashMap<Name, List<JCTree.JCMethodDecl>>();
        classDef.defs.forEach(def -> def.accept(new TreeScanner() {
            @Override public void visitMethodDef(JCTree.JCMethodDecl method) {
                if (method.sym.getAnnotation(DoNotTranspile.class) != null) return;

                var name = method.getName();
                var group = groups.computeIfAbsent(name, __ -> new ArrayList<>());
                group.add(method);
            }

            @Override public void visitClassDef(JCTree.JCClassDecl tree) { }
        }));
        var extendClause = classDef.extending;
        if (extendClause != null) {
            var superClass = extendClause.type.tsym.toString().replace(".", "_");
            out.addDependency(extendClause.type.tsym.toString() + ".js");
            out.write(" extends ");
            out.write(superClass);
        } else {
            var constructors = groups.get(names.fromString("<init>"));
            if (constructors != null) {
                var visitor = new HasSuperConstructorCall(super.names);
                for (var method : constructors) method.accept(visitor);
                if (visitor.hasSuperConstructorCall) out.write(" extends Object");
            }
        }

        out.writeCBOpen(true);

        withClass(classDef, groups, () -> {
            classDef.defs.stream()
                .filter(d -> !(d instanceof JCTree.JCBlock)).filter(d -> !(isInnerEnum && d.type.tsym.isStatic() && d.type.tsym.isFinal()))
                .forEach(d -> d.accept(this));
        });

        out.writeCBEnd(!classDef.sym.isAnonymous());

        if (isInnerEnum && classDef.defs.stream()
            .filter(d -> !(d instanceof JCTree.JCBlock)).anyMatch(d -> d.type.tsym.isStatic() && d.type.tsym.isFinal())) {

            out.write("static {\n");
            out.deepIn();
            withClass(classDef, groups, () -> classDef.defs.stream()
                .filter(d -> !(d instanceof JCTree.JCBlock)).filter(d -> d.type.tsym.isStatic() && d.type.tsym.isFinal())
                .forEach(d -> d.accept(this)));
            out.writeCBEnd(!classDef.sym.isAnonymous());
        }
    }
}