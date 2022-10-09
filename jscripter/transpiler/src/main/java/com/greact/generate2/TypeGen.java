package com.greact.generate2;

import com.greact.generate.util.CompileException;
import com.greact.generate2.lookahead.HasSuperConstructorCall;
import com.greact.model.DoNotTranspile;
import com.greact.model.ErasedInterface;
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
        if (classDef.extending != null && classDef.extending.type.tsym.getAnnotation(JSNativeAPI.class) != null)
            throw new CompileException(CompileException.ERROR.PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API,
                "Prohibition of inheritance for @JSNativeAPI classes");

        var isInnerEnum = classDef.sym.isEnum() && classDef.sym.owner.getKind().isClass();
        var cssRequire = classDef.sym.getAnnotation(Require.CSS.class);
        var isInterface = classDef.getKind() == Tree.Kind.INTERFACE;
        var erasedInterface = classDef.sym.getAnnotation(ErasedInterface.class);

        if (cssRequire != null)
            for (var dep : cssRequire.value())
                out.addDependency(dep);

        if (isInterface) {
            if (erasedInterface != null) {
                if (classDef.defs.stream().anyMatch(n -> ((JCTree.JCMethodDecl) n).sym.isDefault() &&
                    ((JCTree.JCMethodDecl) n).sym.getAnnotation(DoNotTranspile.class) == null))
                    throw new CompileException(CompileException.ERROR.THE_METHOD_MUST_BE_DECLARED_AS_DO_NOT_TRANSPILE,
                        """
                            In @ErasedInterface each default method should be annotated with @DoNotTranspile
                            """);
                if (!classDef.implementing.isEmpty() &&
                    classDef.implementing.get(0).type.tsym.getAnnotation(ErasedInterface.class) == null)
                    throw new CompileException(CompileException.ERROR.ERASED_INTERFACE_CAN_BE_INHERITED_ONLY_FROM_ERASED_INTERFACE,
                        """
                            Erased interface can be inherited only from erased interface
                            """);
                return;
            }

            var interfaceName = classDef.type.tsym.toString().replace(".", "_");

            out.write("const _" + interfaceName + " = (superclass) => class "
                + interfaceName
                + " extends ");

            if (classDef.implementing.isEmpty()) out.write("superclass");
            else {
                if (classDef.implementing.get(0).type.tsym.getAnnotation(ErasedInterface.class) != null)
                    throw new CompileException(CompileException.ERROR.ERASED_INTERFACE_CAN_BE_INHERITED_ONLY_FROM_ERASED_INTERFACE,
                        """
                            Erased interface can be inherited only from erased interface
                            """);
                out.write("_" + classDef.implementing.get(0).type.tsym.toString().replace(".", "_") + "(superclass)");
            }

            out.writeCBOpen(true);
            out.write("__iface_instance__(iface)");
            out.writeCBOpen(true);
            out.write("return (iface === _"
                + interfaceName
                + " || (typeof super.__iface_instance__ !== \"undefined\" && super.__iface_instance__(iface)));");
            out.writeNL();
            out.deepOut();
            out.write("}");
            out.writeNL();

            var groups = new HashMap<Name, List<JCTree.JCMethodDecl>>();
            classDef.defs.forEach(def -> def.accept(new TreeScanner() {
                @Override public void visitMethodDef(JCTree.JCMethodDecl method) {
                    if (method.sym.getAnnotation(DoNotTranspile.class) != null || !method.sym.isDefault())
                        return;

                    var name = method.getName();
                    var group = groups.computeIfAbsent(name, __ -> new ArrayList<>());
                    group.add(method);
                }

                @Override public void visitClassDef(JCTree.JCClassDecl tree) { }
            }));

            withClass(classDef, groups, () -> classDef.defs.forEach(d -> d.accept(this)));

            out.deepOut();
            out.write("};");
            out.writeNL();

            return;
        }

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
        var implementClause = classDef.implementing;

        if (implementClause.stream().anyMatch(n -> n.type.tsym.getAnnotation(ErasedInterface.class) != null))
            throw new CompileException(CompileException.ERROR.CLASS_CANNOT_BE_INHERITED_FROM_ERASED_INTERFACE, """
                Class cannot be inherited from ErasedInterface
                """);

        if (extendClause != null) {
            var superClass = extendClause.type.tsym.toString().replace(".", "_");
            out.addDependency(extendClause.type.tsym.toString() + ".js");
            out.write(" extends ");
            if (!implementClause.isEmpty()) {
                implementClause.forEach(n -> out.write(n.type.tsym.toString().replace(".", "_") + "("));
                out.write(superClass);
                implementClause.forEach(n -> out.write(")"));
            } else out.write(superClass);
        } else if (!implementClause.isEmpty()) {
            out.write(" extends ");
            implementClause.forEach(n -> out.write(n.type.tsym.toString().replace(".", "_") + "("));
            out.write("Object");
            implementClause.forEach(n -> out.write(")"));
        } else {
            var constructors = groups.get(names.fromString("<init>"));
            if (constructors != null) {
                var visitor = new HasSuperConstructorCall(super.names);
                for (var method : constructors) method.accept(visitor);
                if (visitor.hasSuperConstructorCall) out.write(" extends Object");
            }
        }

        out.writeCBOpen(true);

        withClass(classDef, groups, () -> classDef.defs.stream()
            .filter(d -> !(d instanceof JCTree.JCBlock)).filter(d -> !(isInnerEnum && d.type.tsym.isStatic() && d.type.tsym.isFinal()))
            .forEach(d -> d.accept(this)));

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