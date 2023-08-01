package jstack.jscripter.transpiler.generate2;

import jstack.jscripter.transpiler.generate.util.CompileException;
import jstack.jscripter.transpiler.generate2.lookahead.HasSuperConstructorCall;
import jstack.jscripter.transpiler.model.DoNotTranspile;
import jstack.jscripter.transpiler.model.ErasedInterface;
import jstack.jscripter.transpiler.model.JSNativeAPI;
import jstack.jscripter.transpiler.model.Require;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
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

    @Override
    public void visitSkip(JCTree.JCSkip that) { }

    @Override public void visitClassDef(JCTree.JCClassDecl classDef) {
//        if (classDef.extending != null && classDef.sym.getAnnotation(JSNativeAPI.class) == null
//            && classDef.extending.type.tsym.getAnnotation(JSNativeAPI.class) != null)
//            throw new CompileException(CompileException.ERROR.PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API,
//                "Prohibition of inheritance for @JSNativeAPI classes");

        if (classDef.sym.getAnnotation(JSNativeAPI.class) != null) return;

        var isEnum = classDef.sym.isEnum();
        var cssRequire = classDef.sym.getAnnotation(Require.CSS.class);
        var isInterface = classDef.getKind() == Tree.Kind.INTERFACE;
        var isErasedInterface = isAnnotatedErasedInterface(classDef.sym);
        var isFunctionalInterface = classDef.sym.getAnnotation(FunctionalInterface.class) != null;

        if (cssRequire != null)
            for (var dep : cssRequire.value())
                out.addDependency(dep);

        if (isInterface) {
            if (isErasedInterface) {
                if (classDef.defs.stream().anyMatch(n -> ((JCTree.JCMethodDecl) n).sym.isDefault() &&
                    !isMethodAnnotatedDoNotTranspile((JCTree.JCMethodDecl) n)))
                    throw new CompileException(CompileException.ERROR.THE_METHOD_MUST_BE_DECLARED_AS_DO_NOT_TRANSPILE,
                        """
                            In @ErasedInterface each default method must be annotated with @DoNotTranspile
                            """);
                if (!classDef.implementing.isEmpty() &&
                    classDef.implementing.stream().anyMatch(n -> !isAnnotatedErasedInterface((Symbol.ClassSymbol) TreeInfo.symbol(n))))
                    throw new CompileException(CompileException.ERROR.ERASED_INTERFACE_CAN_EXTEND_ONLY_ERASED_INTERFACE,
                        """
                            Erased interface can be inherited only from erased interface
                            """);
                return;
            }

            if (isFunctionalInterface) {
                if (classDef.defs.stream().anyMatch(n -> ((JCTree.JCMethodDecl) n).sym.isDefault()))
                    throw new CompileException(CompileException.ERROR.THE_METHOD_CANNOT_BE_DECLARED_DEFAULT,
                        """
                            The method cannot be declared as default
                            """);
                if (classDef.implementing.stream().anyMatch(n -> n.type.tsym.getAnnotation(FunctionalInterface.class) == null))
                    throw new CompileException(CompileException.ERROR.FUNCTIONAL_INTERFACE_CAN_EXTEND_ONLY_FUNCTIONAL_INTERFACE,
                        """
                            Functional interface can extend only functional interface
                            """);
                return;
            }

            out.write("const _");
            out.replaceSymbolAndWrite(classDef.type.tsym.getQualifiedName(), '.', '_');
            out.write(" = (superclass) => class ");
            out.replaceSymbolAndWrite(classDef.type.tsym.getQualifiedName(), '.', '_');
            out.write(" extends ");

            if (classDef.implementing.isEmpty()) out.write("superclass");
            else {
                if (isAnnotatedErasedInterface((Symbol.ClassSymbol) TreeInfo.symbol(classDef.implementing.get(0))))
                    throw new CompileException(CompileException.ERROR.ERASED_INTERFACE_CAN_EXTEND_ONLY_ERASED_INTERFACE,
                        """
                            Erased interface can be inherited only from erased interface
                            """);
                out.write("_");
                out.replaceSymbolAndWrite(classDef.implementing.get(0).type.tsym.getQualifiedName(), '.', '_');
                out.write("(superclass)");
            }

            out.writeCBOpen(true);
            out.write("__iface_instance__(iface)");
            out.writeCBOpen(true);
            out.write("return (iface === _");
            out.replaceSymbolAndWrite(classDef.type.tsym.getQualifiedName(), '.', '_');
            out.write(" || (typeof super.__iface_instance__ !== \"undefined\" && super.__iface_instance__(iface)));");
            out.writeNL();
            out.deepOut();
            out.write("}");
            out.writeNL();

            var groups = new HashMap<Name, List<JCTree.JCMethodDecl>>();
            classDef.defs.forEach(def -> def.accept(new TreeScanner() {
                @Override public void visitMethodDef(JCTree.JCMethodDecl method) {
                    if (isMethodAnnotatedDoNotTranspile(method) || !method.sym.isDefault())
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

        if (!classDef.type.tsym.isAnonymous() &&
            classDef.sym.owner instanceof Symbol.ClassSymbol) {

            if (!classDef.sym.isStatic())
                throw new RuntimeException("Cannot compile non static inner classes yet");

            out.write("static ");
            out.write(classDef.getSimpleName());
            out.write(" = class");
        } else {
            out.write("class");
            if (!classDef.type.tsym.isAnonymous()) {
                out.write(" ");
                out.replaceSymbolAndWrite(cu.getPackage().getPackageName().type.tsym.getQualifiedName(), '.', '_');
                out.write("_");
                out.write(classDef.getSimpleName());
            }
        }

        var groups = new HashMap<Name, List<JCTree.JCMethodDecl>>();
        classDef.defs.forEach(def -> def.accept(new TreeScanner() {
            @Override public void visitMethodDef(JCTree.JCMethodDecl method) {
                if (isMethodAnnotatedDoNotTranspile(method)) return;

                var name = method.getName();
                var group = groups.computeIfAbsent(name, __ -> new ArrayList<>());
                group.add(method);
            }

            @Override public void visitClassDef(JCTree.JCClassDecl tree) { }
        }));
        var implementClause = classDef.implementing.stream()
            .filter(n -> !isAnnotatedErasedInterface((Symbol.ClassSymbol) TreeInfo.symbol(n)))
            .toList();

        if (classDef.extending != null) {
            var extendClause = classDef.extending.type;
            var stdShimType = stdShim.findShimmedType(extendClause);

            if (stdShimType != null) extendClause = stdShimType;
            var isJsNativeApi = extendClause.tsym.getAnnotation(JSNativeAPI.class) != null;

            out.addDependency(extendClause.tsym.toString() + ".js");
            out.write(" extends ");
            if (!implementClause.isEmpty()) {
                out.write("_");
                implementClause.forEach(n -> {
                    out.replaceSymbolAndWrite(n.type.tsym.getQualifiedName(), '.', '_');
                    out.write("(");
                });
            }

            out.replaceSymbolAndWrite(
                isJsNativeApi
                    ? extendClause.tsym.getSimpleName()
                    : extendClause.tsym.getQualifiedName(),
                '.', '_'
            );
            implementClause.forEach(n -> out.write(")"));
        } else if (!implementClause.isEmpty()) {
            out.write(" extends _");
            implementClause.forEach(n -> {
                out.replaceSymbolAndWrite(n.type.tsym.getQualifiedName(), '.', '_');
                out.write("(");
            });
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

        if (isEnum) {
            out.write("static __Inst = class extends Object");
            out.writeCBOpen(true);
        }

        withClass(classDef, groups, () ->
            classDef.defs.stream()
                .filter(d -> !(d instanceof JCTree.JCBlock))
                .filter(d ->
                    !(isEnum && d instanceof JCTree.JCVariableDecl)
                )
                .forEach(d -> d.accept(this))
        );

        if (isEnum) {
            out.writeCBEnd(true);

            classDef.defs.forEach(d -> {
                if (!(d instanceof JCTree.JCVariableDecl vd)) return;

                if (vd.sym.isStatic()) {
                    out.write("static ");
                    out.write(((JCTree.JCVariableDecl) d).name);
                    out.write(" = new this.__Inst");
                    out.mkString(
                        ((JCTree.JCNewClass) vd.init).args,
                        arg -> arg.accept(this),
                        "(", ", ", ");"
                    );
                    out.writeNL();
                }
            });
            out.writeNL();
            out.write("static __match(self)");
            out.writeCBOpen(true);
            out.write("switch(self)");
            out.writeCBOpen(true);

            classDef.defs.forEach(d -> {
                if (!(d instanceof JCTree.JCVariableDecl vd)) return;

                if (vd.sym.isStatic()) {
                    out.write("case '");
                    out.write(vd.name);
                    out.write("': return this.");
                    out.write(vd.name);
                    out.write(";");
                    out.writeNL();
                }
            });

            out.writeCBEnd(true);
            out.writeCBEnd(true);

            classDef.defs.forEach(d -> {
                if (!(d instanceof JCTree.JCVariableDecl vd)) return;
                if (vd.sym.isStatic()) return;

                out.write("static ");
                out.write(vd.name);
                out.write("(self)");
                out.writeCBOpen(true);
                out.write("return __match(self).");
                out.write(vd.name);
                out.writeLn(";");
                out.writeCBEnd(true);
            });
            // TODO: сделать поддержку методов (с учетом перегрузки)
        }

        out.writeCBEnd(!classDef.sym.isAnonymous());
    }

    boolean isMethodAnnotatedDoNotTranspile(JCTree.JCMethodDecl methodDeclaration) {
        return methodDeclaration.sym.getAnnotation(DoNotTranspile.class) != null;
    }

    boolean isAnnotatedErasedInterface(Symbol.ClassSymbol classSymbol) {
        return classSymbol.getAnnotation(ErasedInterface.class) != null;
    }
}
