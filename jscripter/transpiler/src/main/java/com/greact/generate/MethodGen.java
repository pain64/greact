package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.CompileException;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;
import com.greact.model.async;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.*;
import java.util.Comparator;
import java.util.List;
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

    void group(boolean isOverloaded, boolean hasInSuper, boolean isAsyncInSuper, boolean isAsyncLocal, boolean isStatic,
               List<Pair<Integer, ExecutableElement>> group) {
        if (group.isEmpty()) return;
        if (group.stream().allMatch(m -> m.snd.getModifiers().contains(Modifier.NATIVE))) return;

        var name = group.get(0).snd.getSimpleName().toString();
        var isConstructor = name.equals("<init>");
        out.write(2, "");


        if (hasInSuper) {
            if (isAsyncInSuper) {
                for (var pair : group)
                    if (pair.snd.getAnnotation(async.class) != null)
                        throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                            method already declared as @async in supertype or interface
                            """);
            } else {
                if (isAsyncLocal)
                    for (var pair : group)
                        if (pair.snd.getAnnotation(async.class) != null)
                            throw new CompileException(CANNOT_BE_DECLARED_AS_ASYNC, """
                                method cannot be declared as @async due to:
                                  - NOT declared as @async in supertype or interface
                                """);
            }
        } else {
            if (isAsyncLocal)
                for (var pair : group)
                    if (pair.snd.getAnnotation(async.class) == null)
                        throw new CompileException(MUST_BE_DECLARED_AS_ASYNC, """
                            method must be declared as @async due to:
                              - has overloaded siblings declared as @async
                            """);
        }

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
            out.write(0, param.getSimpleName().toString()), prefix, ", ", ") {\n");

        // FIXME: PIZDATION END
        this.stmtGen = new StatementGen(out, new MContext(ctx, isAsync));

        final Consumer<Integer> defaultConstructLocals;
        if (isConstructor) {
            var fields = ctx.typeEl().getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .filter(el -> !el.getModifiers().contains(Modifier.STATIC))
                .collect(Collectors.toList());

            defaultConstructLocals = (deep) ->
                fields.forEach(field -> {
                    // FIXME: deduplicate with TypeGen
                    var varDecl = (JCTree.JCVariableDecl) ctx.trees().getTree(field);

                    out.write(deep, "this.");
                    out.write(0, varDecl.getName().toString());
                    out.write(0, " = ");

                    if (varDecl.getInitializer() != null)
                        stmtGen.exprGen.expr(4, varDecl.getInitializer());
                        //else if (varDecl.getType().type.tsym.getAnnotation(Integral.class) != null)
                    else if (varDecl.getType().type.isIntegral())
                        out.write(0, "0");
                    else
                        out.write(0, "null");

                    out.write(0, "\n");
                });
        } else defaultConstructLocals = this::NOP;

        if (isOverloaded) {
            out.write(4, "switch($over) {\n");
            group.forEach(m -> {
                out.write(6, "case ");
                out.write(0, String.valueOf(m.fst));
                out.write(0, ":\n");

                // FIXME: args renaming required

                var statements = ctx.trees().getTree(m.snd).getBody().getStatements();
                if (!statements.isEmpty()) {
                    stmtGen.stmt(8, statements.get(0));
                    out.write(0, "\n");
                }

                defaultConstructLocals.accept(8);

                statements.stream().skip(1).forEach(stmt -> {
                    stmtGen.stmt(8, stmt);
                    out.write(0, "\n");
                });

                if (statements.isEmpty() ||
                    !(statements.get(statements.size() - 1) instanceof ReturnTree))
                    out.write(8, "break\n");
            });

            if (!isConstructor && hasInSuper) {
                out.write(6, "default:\n");
                out.write(8, "return super.");
                out.write(0, name);
                out.write(0, ".apply(this, arguments)\n");
            }
            out.write(4, "}\n");
        } else {
            var method = group.get(0).snd;
            // FIXME: deduplicate code
            var statements = ctx.trees().getTree(method).getBody().getStatements();

            if (!statements.isEmpty()) {
                stmtGen.stmt(4, statements.get(0));
                out.write(0, "\n");
            }

            defaultConstructLocals.accept(4);
            statements.stream().skip(1).forEach(stmt -> {
                stmtGen.stmt(4, stmt);
                out.write(0, "\n"); // FIXME: может быть пусть stmtGen сам ставит \n после каждого stmt
            });
        }

        out.write(2, "}");
    }

    void method(Pair<Name, List<ExecutableElement>> group) {
        var types = Types.instance(ctx.context());
        var table = Overloads.table(types, ctx.typeEl(), group.fst);

        group(table.isOverloaded(), table.hasInSuper(), table.isAsyncInSuper(), table.isAsyncLocal(), true, table.staticMethods());
        group(table.isOverloaded(), table.hasInSuper(), table.isAsyncInSuper(), table.isAsyncLocal(), false, table.methods());
    }
}
