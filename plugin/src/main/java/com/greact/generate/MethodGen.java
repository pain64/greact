package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;
import com.greact.generate.util.Overloads;

import javax.lang.model.element.ExecutableElement;

public class MethodGen {
    final TContext ctx;
    final JSOut out;
    final StatementGen stmtGen;

    public MethodGen(TContext ctx, JSOut out) {
        this.ctx = ctx;
        this.out = out;
        stmtGen = new StatementGen(out, ctx);
    }

    void method(ExecutableElement method) {
        var info = Overloads.findMethod(ctx.typeEl(), method);

        out.write(2, info.isStatic() ? "static " : "");
        out.write(0, method.getSimpleName().toString());
        if (info.isOverloaded())
            out.write(0, "$" + info.n());

        out.mkString(method.getParameters(), param ->
            out.write(0, param.getSimpleName().toString()), "(", ", ", ")");

        stmtGen.block(2, ctx.trees().getTree(method).getBody());
    }
}
