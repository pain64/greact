package com.over64.greact;

import com.greact.TranspilerPlugin;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

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
                        TypeSafeSQLCallFinder.executor.shutdown();
                        TypeSafeSQLCallFinder.executor.awaitTermination(10, TimeUnit.SECONDS);
                        System.out.println("GREACT COMPILATION DONE!!!");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException("Too long waiting from database");
                    }
                }
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                    // FIXME: делаем дорогую инициализацию для каждого CompilationUnit???

                    new TypeSafeSQLCallFinder(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());

                    var t1 = System.currentTimeMillis();
                    new CodeViewPlugin(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());
                    var t2 = System.currentTimeMillis();
                    new RPCPlugin(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());
                    var t3 = System.currentTimeMillis();
                    new MarkupPlugin2(context).apply((JCTree.JCCompilationUnit) e.getCompilationUnit());
                    var t4 = System.currentTimeMillis();


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
                    var t5 = System.currentTimeMillis();

                    System.out.println("for " + e.getCompilationUnit().getSourceFile() +
                        "\ncode_view_plugin: " + (t2 - t1) + "ms" +
                        "\nrpc_plugin      : " + (t3 - t2) + "ms" +
                        "\nmarkup_plugin   : " + (t4 - t3) + "ms" +
                        "\npatch_file      : " + (t5 - t4) + "ms"
                    );
                }
            }
        });

        new TranspilerPlugin().init(task, strings);
    }
}