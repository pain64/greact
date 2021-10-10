package com.over64.greact;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;

public class MarkupPlugin2 {
    final Context context;
    final JavacProcessingEnvironment env;
    final Symtab symtab;
    final Names names;
    final Types types;
    final TreeMaker maker;

    final ViewEntryFinder viewEntryFinder;
    final EffectCallFinder effectCallFinder;
    final NewClassPatcher newClassPatcher;


    public MarkupPlugin2(Context context) {
        this.context = context;
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.maker = TreeMaker.instance(context);

        this.viewEntryFinder = new ViewEntryFinder(context);
        this.effectCallFinder = new EffectCallFinder(context);
        this.newClassPatcher = new NewClassPatcher(context);

     //   this.symbols = new MarkupPlugin.Symbols();
    }

    public void apply(JCTree.JCCompilationUnit cu) {
        var effectMap = effectCallFinder.find(cu);
        viewEntryFinder.find(cu)
            .forEach(ce -> {
                var effectsForClass = effectMap.getOrDefault(ce.classDecl(), new ArrayList<>());
                newClassPatcher.patch(ce, effectsForClass);
            });
    }
}
