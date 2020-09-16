package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MethodGen {
    final TContext ctx;
    final JSOut out;
    final StatementGen stmtGen;

    public MethodGen(TContext ctx, JSOut out) {
        this.ctx = ctx;
        this.out = out;
        stmtGen = new StatementGen(out, ctx);
    }

    void defaultConstructor(Iterator<VariableElement> fields) {
        out.mkString(fields, field -> {
            var varDecl = (JCTree.JCVariableDecl) ctx.trees().getTree(field);
            out.write(4, "this.");
            out.write(0, varDecl.getName().toString());
            out.write(0, " = ");

            if (varDecl.getInitializer() != null)
                stmtGen.exprGen.expr(4, varDecl.getInitializer());
            else if (varDecl.getType().type.isIntegral())
                out.write(0, "0");
            else
                out.write(0, "null");

        }, "  constructor() {\n", "\n", "\n  }");
    }

    void group(boolean isOverloaded, boolean hasInSuper, boolean isStatic,
               List<Pair<Integer, ExecutableElement>> group) {
        if (group.isEmpty()) return;
        var name = group.get(0).snd.getSimpleName().toString();

        var staticPrefix = isStatic ? "static " : "";
        out.write(2, staticPrefix);
        out.write(0, name);

        var params = group.stream()
            .map(p -> p.snd.getParameters())
            .max(Comparator.comparingInt(List::size))
            .orElseThrow(() -> new IllegalStateException("unreachable"));

        var prefix = isOverloaded ? "($over, " : "(";
        out.mkString(params, param ->
            out.write(0, param.getSimpleName().toString()), prefix, ", ", ") {\n");

        if (isOverloaded) {
            out.write(4, "switch($over) {\n");
            group.forEach(m -> {
                out.write(6, "case ");
                out.write(0, String.valueOf(m.fst));
                out.write(0, ":\n");

                var statements = ctx.trees().getTree(m.snd).getBody().getStatements();
                statements.forEach(stmt -> {
                    stmtGen.stmt(8, stmt);
                    out.write(0, "\n"); // FIXME: может быть пусть stmtGen сам ставит \n после каждого stmt
                });

                if (statements.isEmpty() ||
                    !(statements.get(statements.size() - 1) instanceof ReturnTree))
                    out.write(8, "break\n");
            });

            if (hasInSuper) {
                out.write(6, "default:\n");
                out.write(8, "return super.");
                out.write(0, name);
                out.write(0, ".apply(this, arguments)\n");
            }
            out.write(4, "}\n");
        } else {
            var method = group.get(0).snd;
            var statements = ctx.trees().getTree(method).getBody().getStatements();
            statements.forEach(stmt -> {
                stmtGen.stmt(4, stmt);
                out.write(0, "\n"); // FIXME: может быть пусть stmtGen сам ставит \n после каждого stmt
            });
        }

        out.write(2, "}");
    }

    void method(Pair<Name, List<ExecutableElement>> group) {
        if (group.fst.toString().equals("<init>")) return;

        var types = Types.instance(ctx.context());
        var table = Overloads.table(types, ctx.typeEl(), group.fst);

        group(table.isOverloaded(), table.hasInSuper(), true, table.staticMethods());
        group(table.isOverloaded(), table.hasInSuper(), false, table.methods());


//        var info = Overloads.findMethod(ctx.typeEl(), method);
//
//        var superType = ((Type) ctx.typeEl().getSuperclass()).tsym;
//        var overloads = superType.getEnclosedElements().stream()
//            .filter(el -> el instanceof ExecutableElement && el.getSimpleName() == method.getSimpleName())
//            .map(el -> (ExecutableElement) el)
//            .collect(Collectors.toList());
//
//        if (!overloads.isEmpty()) {
//            var xx = ((Symbol.MethodSymbol) method).overrides((Symbol) overloads.get(0), superType, Types.instance(ctx.context()), true);
//        }
//
////        out.write(2, info.isStatic() ? "static " : "");
//        // out.write(0, method.getSimpleName().toString());
//        if (info.isOverloaded())
//            out.write(0, "$" + info.n());
//
//        out.mkString(method.getParameters(), param ->
//            out.write(0, param.getSimpleName().toString()), "(", ", ", ")");
//
//        stmtGen.block(2, ctx.trees().getTree(method).getBody());
    }

}
