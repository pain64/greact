package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;
import com.greact.shim.java.lang.model.Integral;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MethodGen {
    final TContext ctx;
    final JSOut out;
    final StatementGen stmtGen;

    public MethodGen(TContext ctx, JSOut out) {
        this.ctx = ctx;
        this.out = out;
        stmtGen = new StatementGen(out, ctx);
    }

    void NOP(int deep) {}

    void group(boolean isOverloaded, boolean hasInSuper, boolean isStatic,
               List<Pair<Integer, ExecutableElement>> group) {
        if (group.isEmpty()) return;
        var name = group.get(0).snd.getSimpleName().toString();
        var isConstructor = name.equals("<init>");

        var staticPrefix = isStatic ? "static " : "";
        out.write(2, staticPrefix);
        out.write(0, isConstructor ? "constructor" : name);

        var params = group.stream()
            .map(p -> p.snd.getParameters())
            .max(Comparator.comparingInt(List::size))
            .orElseThrow(() -> new IllegalStateException("unreachable"));

        var prefix = isOverloaded ? "($over, " : "(";
        out.mkString(params, param ->
            out.write(0, param.getSimpleName().toString()), prefix, ", ", ") {\n");

        final Consumer<Integer> defaultConstructLocals;
        if (isConstructor) {
            var fields = ctx.typeEl().getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .collect(Collectors.toList());

            defaultConstructLocals = (deep) ->
                fields.forEach(field -> {
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

        group(table.isOverloaded(), table.hasInSuper(), true, table.staticMethods());
        group(table.isOverloaded(), table.hasInSuper(), false, table.methods());
    }
}
