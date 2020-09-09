package com.greact.generate;

import com.greact.generate.TypeGen.TContext;
import com.greact.generate.util.JSOut;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.stream.IntStream;

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
        var params = method.getParameters();
        var group = ctx.overloadMap().get(method.getSimpleName().toString());

        var overloadSuffix = "";
        if (group.size() != 1) // has overloads
            overloadSuffix = "$" + IntStream.range(0, group.size()).filter(i -> {
                var mi = group.get(i);
                if (mi.argTypes().size() != params.size()) return false;

                for (var j = 0; j < params.size(); j++) {
                    var myArgType = ctx.trees().getTypeMirror(ctx.trees().getPath(params.get(j))).toString();
                    if (!myArgType.equals(mi.argTypes().get(j))) return false;
                }

                return true;
            }).findFirst().getAsInt();

        out.write(2, method.getModifiers()
            .contains(Modifier.STATIC) ? "static " : "");
        out.write(0, method.getSimpleName().toString());
        out.write(0, overloadSuffix);

        out.mkString(method.getParameters(), param ->
            out.write(0, param.getSimpleName().toString()), "(", ", ", ")");

        stmtGen.block(2, ctx.trees().getTree(method).getBody());
    }
}
