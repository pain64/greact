package com.greact;

import com.greact.model.Component;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;


public class GReactPlugin implements Plugin {
    @Override
    public String getName() {
        return "GReact";
    }

//    ProcessingEnvironment getEnv(JavacTask task) {
//        JavacProcessingEnvironment.instance(((BasicJavacTask) task).getContext());
//        try {
//            Class taskClass = Class.forName("com.sun.tools.javac.api.BasicJavacTask");
//
//            Field contextField = taskClass.getDeclaredField("context");
//            contextField.setAccessible(true);
//            Object context = contextField.get(task);
//
//            Class envClass = JavacProcessingEnvironment.class;
//            return (ProcessingEnvironment) envClass.getDeclaredMethod("instance").invoke(null, context);
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//
//    }

    @Override
    public void init(JavacTask task, String... strings) {
        var env = JavacProcessingEnvironment.instance(((BasicJavacTask) task).getContext());

        task.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.GENERATE)
                    return;

                System.out.println("before generate for: " + e);

                try {
                    var jsFile = env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                        e.getCompilationUnit().getPackageName().toString(),
                        e.getTypeElement().getSimpleName() + ".js");
                    
                    var writer = jsFile.openWriter();
                    writer.write("super puper js code");
                    writer.close();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.ANALYZE)
                    return;

                System.out.println("after analyze for: " + e);
                e.getCompilationUnit().getImports().forEach(i -> {
                    //ImportTree
                });

                // System.out.println("finish analyze for: " + e);

//                Element el = null;
//                var jctree = env.getElementUtils().getTree(el);
//                el.getKind()
//                jctree.

//                var cu = e.getCompilationUnit();
//                var docTree = DocTrees.instance(task);
//                var pkgName = Optional.ofNullable(cu.getPackageName())
//                    .map(Objects::toString).orElse("");
//
//                cu.accept(new TreeScanner<Void, Void>() {
//                    @Override
//                    public Void visitClass(ClassTree classNode, Void aVoid) {
//                        var compAnnotation =
//                            classNode.getModifiers().getAnnotations().stream()
//                                .filter(a -> Component.class.getSimpleName()
//                                    .equals(a.getAnnotationType().toString()))
//                                .findFirst();
//
//                        if (compAnnotation.isPresent()) {
//                            System.out.println("instrument for: " +
//                                pkgName + "." + classNode.getSimpleName());
//
//
////                            Log.instance(((BasicJavacTask) task).getContext())
////                                .error(JCDiagnostic.DiagnosticFlag.API, pos,  CompilerProperties.Errors.ProcMessager(msg.toString()));
//
//                            env.getMessager().printMessage(Diagnostic.Kind.ERROR, "oops");
//
//                            classNode.getMembers().forEach(member -> {
//                                var doc = docTree.getDocCommentTree(docTree.getPath(cu, member));
//                                if (doc != null)
//                                    System.out.println(doc.toString());
//
//                            });
//                        }
//
//                        return aVoid; // super.visitClass(classNode, aVoid);
//                    }
//                }, null);
            }
        });
    }
}