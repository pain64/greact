package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.CompileException;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;
import com.greact.model.DoNotTranspile;
import com.greact.model.async;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.greact.generate.util.CompileException.ERROR.*;

public class MethodGen {
    static record MContext(TContext ctx, boolean isAsync) {
    }

    final TContext ctx;
    final JSOut out;
    StatementGen stmtGen; // FIXME: PIZDATION BEGIN

    public MethodGen(TContext ctx, JSOut out) {
        this.ctx = ctx;
        this.out = out;
    }

    void NOP(int deep) {}

    void group(int deep, boolean isOverloaded, boolean hasInSuper, boolean isAsyncInSuper, boolean isAsyncLocal, boolean isStatic,
               List<Pair<Integer, JCTree.JCMethodDecl>> group) {
        if (group.isEmpty()) return;
        if (group.stream().allMatch(m -> m.snd.sym.getModifiers().contains(Modifier.NATIVE))) return;

        var name = group.get(0).snd.getName().toString();
        var isConstructor = name.equals("<init>");

        if(group.stream().allMatch(p -> p.snd.sym.isAbstract()))
            return;

        if (hasInSuper) {
            if (isAsyncInSuper) {
                for (var pair : group)
                    if (pair.snd.sym.getAnnotation(async.class) != null)
                        throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                            method already declared as @async in supertype or interface
                            """);
            } else {
                if (isAsyncLocal)
                    for (var pair : group)
                        if (pair.snd.sym.getAnnotation(async.class) != null)
                            throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                                method cannot be declared as @async due to:
                                  - NOT declared as @async in supertype or interface
                                """);
            }
        } else {
            if (isAsyncLocal)
                for (var pair : group)
                    if (pair.snd.sym.getAnnotation(async.class) == null)
                        throw new CompileException(MUST_BE_DECLARED_AS_ASYNC, """
                            method must be declared as @async due to:
                              - has overloaded siblings declared as @async
                            """);
        }

        out.write(deep + 2, "");
        if (isStatic) out.write(0, "static ");
        var isAsync = isAsyncInSuper || isAsyncLocal;
        if (isAsync) out.write(0, "async ");
        out.write(0, isConstructor ? "constructor" : name);


        var params = group.stream()
            .map(p -> p.snd.getParameters())
            .max(Comparator.comparingInt(List::size))
            .orElseThrow(() -> new IllegalStateException("unreachable"));

        var prefix = isOverloaded ? "($over, " : "(";
        out.mkString(params, param ->
            out.write(0, param.getName().toString()), prefix, ", ", ") {\n");

        // FIXME: PIZDATION END
        this.stmtGen = new StatementGen(out, new MContext(ctx, isAsync));

        BiFunction<Integer, JCTree.JCVariableDecl, Void> initField = (_deep, varDecl) -> {
            out.write(_deep, "this.");
            out.write(0, varDecl.getName().toString());
            out.write(0, " = ");

            if (varDecl.getInitializer() != null)
                stmtGen.exprGen.expr(4, varDecl.getInitializer());
            else if (varDecl.getType().type.isIntegral())
                out.write(0, "0");
            else
                out.write(0, "null");

            out.write(0, "\n");
            return null;
        };


        final Consumer<Integer> defaultConstructLocals;
        if (isConstructor) {
            var fields = ctx.typeEl().sym.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .filter(el -> !el.getModifiers().contains(Modifier.STATIC))
                .collect(Collectors.toList());

            defaultConstructLocals = _deep ->
                fields.forEach(field -> {
                    // FIXME: deduplicate with TypeGen
                    var varDecl = (JCTree.JCVariableDecl) ctx.trees().getTree(field);
                    if (!((Symbol.VarSymbol) field).isFinal() || varDecl.getInitializer() != null)
                        initField.apply(_deep, varDecl);
                });
        } else defaultConstructLocals = this::NOP;

        BiFunction<Integer, JCTree.JCMethodDecl, Void> recordConstructLocals = (_deep, method) -> {
            if (((Symbol.ClassSymbol) method.sym.owner).isRecord())
                method.params.forEach(varDecl ->
                    out.write(_deep, "this." + varDecl.getName() + " = " + varDecl.getName() + ";\n"));
            return null;
        };

        if (isOverloaded) {
            out.write(deep + 4, "switch($over) {\n");
            group.forEach(m -> {
                out.write(deep + 6, "case ");
                out.write(0, String.valueOf(m.fst));
                out.write(0, ":\n");

                // FIXME: args renaming required

                var statements = m.snd.sym.isAbstract() ?
                    com.sun.tools.javac.util.List.<JCTree.JCStatement>nil() : m.snd.body.stats;
                if (!statements.isEmpty()) {
                    stmtGen.stmt(deep + 8, statements.get(0));
                    out.write(0, "\n");
                }

                defaultConstructLocals.accept(deep + 8);
                recordConstructLocals.apply(deep + 8, m.snd);

                statements.stream().skip(1).forEach(stmt -> {
                    stmtGen.stmt(deep + 8, stmt);
                    out.write(0, "\n");
                });

                if (statements.isEmpty() ||
                    !(statements.get(statements.size() - 1) instanceof ReturnTree))
                    out.write(deep + 8, "break\n");
            });

            if (!isConstructor && hasInSuper) {
                out.write(deep + 6, "default:\n");
                out.write(deep + 8, "return super.");
                out.write(0, name);
                out.write(0, ".apply(this, arguments)\n");
            }
            out.write(deep + 4, "}\n");
        } else {
            var method = group.get(0).snd;
            // FIXME: deduplicate code
            var statements = method.body.stats;

            if (!statements.isEmpty()) { // super constructor call
                stmtGen.stmt(deep + 4, statements.get(0));
                out.write(0, "\n");
            }

            defaultConstructLocals.accept(deep + 4);
            recordConstructLocals.apply(deep + 4, method);
            statements.stream().skip(1).forEach(stmt -> {
                stmtGen.stmt(deep + 4, stmt);
                out.write(0, "\n"); // FIXME: может быть пусть stmtGen сам ставит \n после каждого stmt
            });
        }

        out.write(deep + 2, "}");
    }

    void method(int deep, Pair<Name, List<JCTree.JCMethodDecl>> group) { // FIXME: don't need pair here?
        var types = Types.instance(ctx.context());
        var table = Overloads.table(types, ctx.typeEl().sym, group.fst);
        var staticMethods = group.snd.stream()
            .filter(m -> m.sym.isStatic())
            .map(m -> {
                var pair = table.staticMethods().stream().filter(tm -> tm.snd == m.sym).findFirst().get();
                return new Pair<>(pair.fst, m);
            })
            .collect(Collectors.toList());

        var nonStaticMethods = group.snd.stream()
            .filter(m -> !m.sym.isStatic())
            .map(m -> {
                var pair = table.methods().stream().filter(tm -> tm.snd == m.sym).findFirst().get();
                return new Pair<>(pair.fst, m);
            })
            .collect(Collectors.toList());

        group(deep, table.isOverloaded(), table.hasInSuper(), table.isAsyncInSuper(), table.isAsyncLocal(), true,
            staticMethods);
        group(deep, table.isOverloaded(), table.hasInSuper(), table.isAsyncInSuper(), table.isAsyncLocal(), false, nonStaticMethods);
    }
}
