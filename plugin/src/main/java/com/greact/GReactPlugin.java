package com.greact;

import com.greact.generate.JSGen;
import com.sun.source.util.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import javax.tools.StandardLocation;

import com.sun.tools.javac.api.BasicJavacTask;

import java.io.IOException;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
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

                var cu = e.getCompilationUnit();
                if(!cu.getPackage().getPackageName().toString().equals("js"))
                    return;

                System.out.println("before generate for: " + e + "cu: " + cu);

                try {
                    var jsFile = env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                        cu.getPackageName().toString(),
                        e.getTypeElement().getSimpleName() + ".js");
                    
                    var writer = jsFile.openWriter();
                    new JSGen(writer, cu).genType(0, e.getTypeElement());
                    writer.close();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.ANALYZE)
                    return;

                

//                System.out.println("after analyze for: " + e);
//                e.getCompilationUnit().getImports().forEach(i -> {
//                    //ImportTree
//                });

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