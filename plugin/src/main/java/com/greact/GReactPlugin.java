package com.greact;

import com.greact.generate.TypeGen;
import com.greact.generate.util.JSOut;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Pair;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }

    final String jsCodePackage = "js";
    ShimLibrary shim = new ShimLibrary("com.greact.shim.java.lang", "java.lang");
    List<Pair<BasicJavacTask, TaskEvent>> events = new ArrayList<>();

    void foobar() {

    }

    @Override
    public void init(JavacTask task, String... strings) {
        var context = ((BasicJavacTask) task).getContext();
        var env = JavacProcessingEnvironment.instance(context);

        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() != TaskEvent.Kind.ANALYZE) return;

                var cu = e.getCompilationUnit();
                // FIXME: NPE
                var pkg = cu.getPackage().getPackageName().toString();
                if (!pkg.startsWith(jsCodePackage)) return;


                System.out.println("after analyze for: " + e + "cu: " + cu);
                events.add(Pair.of((BasicJavacTask) task, e));

                try {
                    var jsFile = env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                        cu.getPackageName().toString(),
                        e.getTypeElement().getSimpleName() + ".js");

                    var writer = jsFile.openWriter();
                    new TypeGen(new JSOut(writer), cu, env, context).type(0, e.getTypeElement());
                    writer.close();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

//            @Override
//            public void finished(TaskEvent e) {
//                if (e.getKind() != TaskEvent.Kind.ANALYZE)
//                    return;
//
//

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
//            }
        });
    }
}