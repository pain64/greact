package com.greact.generate;

import com.greact.generate.util.JSOut;
import com.greact.generate.util.JavaStdShim;
import com.greact.model.DoNotTranspile;
import com.greact.model.JSNativeAPI;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class TypeGen {
    public static record TContext(
        JCTree.JCCompilationUnit cu,
        Trees trees,
        Types types,
        Context context,
        JCTree.JCClassDecl typeEl,
        JavaStdShim stdShim,
        JavacProcessingEnvironment env
    ) {
    }

    final JSOut out;
    final JCTree.JCCompilationUnit cu;
    final Trees trees;
    final Context context;
    final JavaStdShim stdShim;
    final JavacProcessingEnvironment env;

    public TypeGen(JSOut out, JCTree.JCCompilationUnit cu, JavacProcessingEnvironment env, Context context,
                   JavaStdShim stdShim) {
        this.out = out;
        this.cu = cu;
        this.trees = Trees.instance(env);
        this.context = context;
        this.stdShim = stdShim;
        this.env = env;
    }

    void initField(int deep, TContext ctx, boolean isStatic, JCTree.JCVariableDecl varDecl) {
        out.write(deep, isStatic ? "static " : "this.");
        out.write(0, varDecl.getName().toString());
        out.write(0, " = ");

        if (varDecl.getInitializer() != null)
            new StatementGen(out, new MethodGen.MContext(ctx, false)).exprGen.expr(deep, varDecl.getInitializer());
        else if (varDecl.getType().type.isIntegral())
            out.write(0, "0");
        else
            out.write(0, "null");

        out.write(0, "\n");
    }

    public void type(int deep, JCTree decl) {
        if (!(decl instanceof JCTree.JCClassDecl)) return;
        var typeDecl = (JCTree.JCClassDecl) decl;

        if (typeDecl.getKind() == Tree.Kind.INTERFACE) return;
        if (typeDecl.sym.getAnnotation(JSNativeAPI.class) != null) return;

        out.write(0, "class ");
        if (!typeDecl.type.tsym.isAnonymous()) {
            out.write(0, cu.getPackage().getPackageName().toString().replace(".", "$"));
            out.write(0, "$");
            out.write(0, typeDecl.getSimpleName().toString());
            out.write(0, " ");
        }

        var extendClause = typeDecl.extending;
        var superClass = "Object";
        if(extendClause != null) {
            superClass = extendClause.type.tsym.toString().replace(".", "$");
            out.dependsOnTypes.add(extendClause.type.tsym.toString() + ".js");
        }
        out.write(0, "extends ");
        out.write(0, superClass);

        var ctx = new TContext(cu, trees, Types.instance(context), context, typeDecl, stdShim, env);
        var mGen = new MethodGen(ctx, out);

        out.write(0, " {\n");

        var staticFields = typeDecl.sym.getEnclosedElements().stream()
            .filter(el -> el.getKind() == ElementKind.FIELD)
            .map(el -> (VariableElement) el)
            .filter(el -> el.getModifiers().contains(Modifier.STATIC)).iterator();

        var hasStaticFields = staticFields.hasNext();
        staticFields.forEachRemaining(field -> {
            var varDecl = (JCTree.JCVariableDecl) ctx.trees().getTree(field);
            initField(deep + 2, ctx, true, varDecl);
        });
        if (hasStaticFields) out.write(0, "\n");

        var initBlock = typeDecl.defs.stream()
            .filter(def -> def instanceof JCTree.JCBlock)
            .map(def -> (JCTree.JCBlock) def)
            .findFirst();

        var methods = new ArrayList<Pair<Name, List<JCTree.JCMethodDecl>>>();

        typeDecl.defs.forEach(def -> def.accept(new TreeScanner() {
            @Override
            public void visitMethodDef(JCTree.JCMethodDecl method) {
                if (method.sym.getAnnotation(DoNotTranspile.class) != null) return;

                var name = method.getName();
                var group = methods.stream()
                    .filter(p -> p.fst.equals(name)).findFirst()
                    .orElseGet(() -> {
                        var newGroup = new Pair<Name, List<JCTree.JCMethodDecl>>(name, new ArrayList<>());
                        methods.add(newGroup);
                        return newGroup;
                    });
                group.snd.add(method);
            }

            @Override
            public void visitClassDef(JCTree.JCClassDecl tree) { }
        }));

        out.mkString(methods, g -> mGen.method(deep, initBlock, g), "", "\n\n", "");
        out.write(0, "\n");
        out.write(deep, "}");
    }
    // ENUM
}
