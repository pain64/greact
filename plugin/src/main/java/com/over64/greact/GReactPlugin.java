package com.over64.greact;

import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.stream.Collectors;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(JavacTask task, String... strings) {
        var context = ((BasicJavacTask) task).getContext();

        task.addTaskListener(new TaskListener() {

            @Override
            public void finished(TaskEvent e) {
                var env = JavacProcessingEnvironment.instance(context);
                var symTab = Symtab.instance(context);

                if (e.getKind() == TaskEvent.Kind.ENTER) {
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    for (var typeDecl : cu.getTypeDecls()) {
                        typeDecl.accept(new TreeScanner() {
                            @Override public void visitApply(JCTree.JCMethodInvocation that) {
                                symTab.getAllClasses().forEach(c -> {
                                    var x = 1;
                                });
                                // Trees.instance(env).getTree(symTab.enterClass(symTab.unnamedModule, Names.instance(context).fromString("js.H1"))).defs
                                var x = 1;
                                super.visitApply(that);
                            }
                        });

                    }
                    var maker = TreeMaker.instance(context);
                    var names = Names.instance(context);
                }
            }
        });
    }
}