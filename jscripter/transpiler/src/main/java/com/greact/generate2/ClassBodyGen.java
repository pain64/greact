package com.greact.generate2;

import com.greact.generate.util.CompileException;
import com.greact.generate.util.Overloads;
import com.greact.generate.util.Overloads.OverloadTable;
import com.greact.model.Static;
import com.greact.model.async;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.List;

import static com.greact.generate.util.CompileException.ERROR.CANNOT_BE_DECLARED_AS_ASYNC;
import static com.greact.generate.util.CompileException.ERROR.MUST_BE_DECLARED_AS_ASYNC;

abstract class ClassBodyGen extends StatementGen {
    @Override public void visitVarDef(JCTree.JCVariableDecl varDef) {
        if (varDef.sym.isStatic()) {
            out.write("static ");
            out.write(varDef.getName().toString());
            out.write(" = ");

            if (varDef.init != null) varDef.init.accept(this);
            else out.write("null");
            out.writeLn(";");
        } else if (!(varDef.sym.owner instanceof Symbol.ClassSymbol))
            // skip class fields & delegate to StatementGen
            super.visitVarDef(varDef);
    }

    void initField(JCTree.JCVariableDecl varDef) {
        out.write("this.");
        out.write(varDef.getName().toString());
        out.write(" = ");

        if (varDef.getInitializer() != null)
            varDef.getInitializer().accept(this);
        else if (varDef.getType().type.isIntegral())
            out.write("0");
        else
            out.write("null");

        out.writeLn(";");
    }

    void initRecordFields(JCTree.JCMethodDecl method) {
        if (((Symbol.ClassSymbol) method.sym.owner).isRecord())
            method.params.forEach(varDef -> {
                out.write("this.");
                out.write(varDef.getName().toString());
                out.write(" = ");
                out.write(varDef.getName().toString());
                out.writeLn(";");
            });
    }

    void writeMethodStatements(JCTree.JCMethodDecl method, boolean hasInit, boolean isAsync) {
        var statements = method.sym.isAbstract()
            ? com.sun.tools.javac.util.List.<JCTree.JCStatement>nil()
            : method.body.stats;

        withAsyncContext(isAsync, () -> {
            if (!statements.isEmpty()) // super constructor invocation
                statements.get(0).accept(this);

            if (hasInit) out.writeLn("__init__();");
            initRecordFields(method);

            statements.stream().skip(1).forEach(stmt -> stmt.accept(this));
        });
    }

    void visitGroup(OverloadTable table, boolean isStatic,
                    List<Pair<Integer, JCTree.JCMethodDecl>> methods) {

        if (methods.isEmpty()) return;
        if (methods.stream().allMatch(m -> m.snd.sym.getModifiers().contains(Modifier.NATIVE)))
            return;
        if (methods.stream().allMatch(m -> m.snd.sym.isAbstract())) return;

        var name = methods.get(0).snd.name.toString();
        var isConstructor = name.equals("<init>");
        var isAsStatic = methods.stream().anyMatch(m -> m.snd.sym.getAnnotation(Static.class) != null);

        if (table.hasInSuper()) {
            if (table.isAsyncInSuper()) {
                for (var pair : methods)
                    if (pair.snd.sym.getAnnotation(async.class) != null)
                        throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                            method already declared as @async in supertype or interface
                            """);
            } else {
                if (table.isAsyncLocal())
                    for (var pair : methods)
                        if (pair.snd.sym.getAnnotation(async.class) != null)
                            throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                                method cannot be declared as @async due to:
                                  - NOT declared as @async in supertype or interface
                                """);
            }
        } else {
            if (table.isAsyncLocal())
                for (var pair : methods)
                    if (pair.snd.sym.getAnnotation(async.class) == null)
                        throw new CompileException(MUST_BE_DECLARED_AS_ASYNC, """
                            method must be declared as @async due to:
                              - has overloaded siblings declared as @async
                            """);
        }


        if (isStatic || isAsStatic) out.write("static ");
        var isAsync = table.isAsyncInSuper() || table.isAsyncLocal();
        if (isAsync) out.write("async ");

        if (isConstructor)
            out.write("constructor");
        else {
            out.write("_");
            out.write(name);
        }

        var params = methods.stream()
            .map(m -> m.snd.getParameters())
            .max(Comparator.comparingInt(List::size))
            .orElseThrow(() -> new IllegalStateException("unreachable"));

        if (!table.isOverloaded())
            out.mkString(params, param -> out.write(param.getName().toString()), "(", ", ", ")");
        else
            out.write("($over, ...__args)");

        out.writeCBOpen(true);

        final boolean hasInit;
        if (isConstructor) {
            var fields = classDef.sym.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .filter(el -> !el.getModifiers().contains(Modifier.STATIC)).iterator();

            var initBlock = classDef.defs.stream()
                .filter(def -> def instanceof JCTree.JCBlock)
                .map(def -> (JCTree.JCBlock) def)
                .findFirst();

            hasInit = (fields.hasNext() && !classDef.sym.isRecord()) || initBlock.isPresent();

            if (hasInit) {
                withAsyncContext(false, () -> { // constructor cannot be async in JS
                    out.write("const __init__ = () =>");
                    out.writeCBOpen(true);
                    fields.forEachRemaining(field -> {
                        var varDef = (JCTree.JCVariableDecl) trees.getTree(field);
                        initField(varDef);
                    });
                    initBlock.ifPresent(block -> block.stats.forEach(stmt -> stmt.accept(this)));
                    out.writeCBEnd(false);
                    out.writeLn(";");
                });
            }
        } else hasInit = false;

        if (table.isOverloaded()) {
            var isFirst = true;
            for (var m : methods) {
                if (isFirst) out.write("if($over === ");
                else out.write(" else if($over === ");
                isFirst = false;

                out.write(String.valueOf(m.fst));
                out.write(")");
                out.writeCBOpen(true);

                if (!m.snd.params.isEmpty()) {
                    out.mkString(m.snd.params, varDef -> out.write(varDef.name.toString()),
                        "const [", ", ", "] = __args;");
                    out.writeNL();
                }
                writeMethodStatements(m.snd, hasInit, isAsync);
                out.writeCBEnd(false);
            }

            if (!isConstructor && table.hasInSuper()) {
                out.writeLn(" else");
                out.deepIn();
                out.write("return super._");
                out.write(name);
                out.writeLn(".apply(this, arguments);");
                out.deepOut();
            } else out.writeNL();
        } else
            writeMethodStatements(methods.get(0).snd, hasInit, isAsync);

        out.writeCBEnd(true);
    }

    List<Pair<Integer, JCTree.JCMethodDecl>> mapMethods(
        List<JCTree.JCMethodDecl> group, List<Pair<Integer, ExecutableElement>> methods) {

        return methods.stream().map(p ->
            new Pair<>(p.fst, group.stream().filter(m -> m.sym == p.snd)
                .findFirst().orElseThrow())).toList();
    }


    @Override public void visitMethodDef(JCTree.JCMethodDecl methodDef) {
        var group = groups.get(methodDef.getName());
        if (group == null) return; // @DoNotTranspile method
        if (methodDef != group.get(0)) return;

        var table = Overloads.table(types, classDef.sym, methodDef.name);
        var nonStaticMethods = mapMethods(group, table.methods());
        var staticMethods = mapMethods(group, table.staticMethods());

        visitGroup(table, true, staticMethods);
        visitGroup(table, false, nonStaticMethods);
    }
}
