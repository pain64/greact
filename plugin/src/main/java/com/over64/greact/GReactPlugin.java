package com.over64.greact;

import com.greact.TranspilerPlugin;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Log;


public class GReactPlugin implements Plugin {

    public static final String NAME = "GReact";

    @Override
    public String getName() {
        return NAME;
    }

    long startedAtMillis;
    JavacTask theTask;

    @Override
    public void init(JavacTask task, String... strings) {
        System.out.println("INIT GREACT PLUGIN");
        theTask = task;
        var context = ((BasicJavacTask) task).getContext();
        var comp = context.get(JavaCompiler.compilerKey);
        var origErrWriter = comp.log.getWriter(Log.WriterKind.ERROR);
        //comp.log.setWriter(new PrintWriter());

        task.addTaskListener(new TaskListener() {

            @Override public void started(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.COMPILATION)
                    startedAtMillis = System.currentTimeMillis();
            }
            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.COMPILATION) {


                    try {
                        //comp.log.
                        var result = comp.errorCount() == 0 ? "success" : "fail";
                        Files.write(Paths.get("/tmp/greact_compiled"),
                            result.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("GREACT COMPILATION DONE!!!");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    // FIXME: делаем дорогую инициализацию для каждого CompilationUnit???
                    System.out.println("before init: " + System.currentTimeMillis());
                    System.out.println("compile for:  " + e.getCompilationUnit().getSourceFile().getName());
                    new RPCPlugin(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());
                    new MarkupPlugin2(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());
                    System.out.println("after init:  " + System.currentTimeMillis());

                    var env = JavacProcessingEnvironment.instance(context);
                    var cu = (JCTree.JCCompilationUnit) e.getCompilationUnit();

                    try {
                        var jsFile = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            cu.getPackageName().toString(),
                            e.getTypeElement().getSimpleName() + ".java.patch");

                        var writer = jsFile.openWriter();
                        writer.write(cu.toString());
                        writer.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    // ((JCTree.JCMethodDecl) ((JCTree.JCClassDecl) cu.defs.get(5)).defs.get(1)).body.stats
                    var zz = 1;
                    // new class file
                }
            }
        });

        new TranspilerPlugin().init(task, strings);
    }
}