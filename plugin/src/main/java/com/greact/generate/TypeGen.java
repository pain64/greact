package com.greact.generate;

import com.greact.generate.util.JSOut;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;

public class TypeGen {
    public static record TContext(
        CompilationUnitTree cu,
        Trees trees,
        Context context,
        TypeElement typeEl
    ) {
    }

    final JSOut out;
    final CompilationUnitTree cu;
    final Trees trees;
    final Context context;

    public TypeGen(JSOut out, CompilationUnitTree cu, JavacProcessingEnvironment env, Context context) {
        this.out = out;
        this.cu = cu;
        this.trees = Trees.instance(env);
        this.context = context;
    }

    public void type(int deep, TypeElement typeEl) {

        if (typeEl.getKind() == ElementKind.INTERFACE)
            return;


        out.write(deep, "class ");
        out.write(deep, cu.getPackage().getPackageName().toString().replace(".", "$"));
        out.write(0, "$");
        out.write(0, typeEl.getSimpleName().toString());

        var extendClause = (JCTree) trees.getTree(typeEl).getExtendsClause();
        if(extendClause != null) {
            out.write(0, " extends ");
            out.write(0, extendClause.type.toString().replace(".", "$"));
        }

        var ctx = new TContext(cu, trees, context, typeEl);
        var mGen = new MethodGen(ctx, out);

        out.write(0, " {\n");

//        var fields = typeEl.getEnclosedElements().stream()
//            .filter(el -> el.getKind() == ElementKind.FIELD)
//            .map(el -> (VariableElement) el);
//        //mGen.defaultConstructor(fields.iterator());
//
//        var constructors = typeEl.getEnclosedElements().stream()
//            .filter(el -> el.getKind() == ElementKind.CONSTRUCTOR)
//            .map(el -> (ExecutableElement) el).iterator();

        //if (constructors.hasNext()) out.write(0, "\n");
        //out.mkString(constructors, mGen::constructor, "", "\n\n", "");

        var methods = new ArrayList<Pair<Name, List<ExecutableElement>>>();

        typeEl.getEnclosedElements().forEach(el -> {
            if (el instanceof ExecutableElement method && method.getKind() == ElementKind.METHOD) {
                var name = method.getSimpleName();
                var group = methods.stream()
                    .filter(p -> p.fst.equals(name)).findFirst()
                    .orElseGet(() -> {
                        var newGroup = new Pair<Name, List<ExecutableElement>>(name, new ArrayList<>());
                        methods.add(newGroup);
                        return newGroup;
                    });
                group.snd.add(method);
            }
        });

//        var methods = typeEl.getEnclosedElements().stream()
//            .filter(el -> el.getKind() == ElementKind.METHOD)
//            .map(el -> (ExecutableElement) el)
//            .collect(Collectors.g)
//            .iterator();

        //if (methods.hasNext()) out.write(0, "\n");
        out.mkString(methods, mGen::method, "", "\n\n", "");

        out.write(0, "\n}");
    }
    // ENUM
}
