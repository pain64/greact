package util;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;

import static java.util.Arrays.asList;

public class AnalyzeAssertionsCompiler {
    public static class AnalyzeAssertionsPlugin implements Plugin {
        static final String NAME = "AnalyzeAssertionsPlugin";
        @Override public String getName() {return NAME;}

        JavacTask theTask;
        @Override public void init(JavacTask task, String... strings) {
            var context = ((BasicJavacTask) task).getContext();
            var klass = strings[0];
            var argsData = strings[1];
            theTask = task;

            final Object args;
            final Object inst;
            Method methodDecl = null;

            try {
                args = new ObjectInputStream(
                    new ByteArrayInputStream(
                        Base64.getDecoder().decode(argsData)))
                    .readObject();
                var cl = Class.forName(klass);
                var constructor = cl.getConstructors()[0];
                inst = constructor.newInstance();

                for (var decl : cl.getDeclaredMethods())
                    if (decl.getName().equals("doAssert"))
                        methodDecl = decl;

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            var mt = methodDecl;
            task.addTaskListener(new TaskListener() {
                @Override public void finished(TaskEvent e) {
                    if (e.getKind() == TaskEvent.Kind.ANALYZE)
                        try {
                           mt.invoke(inst, context, e.getCompilationUnit(), args);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                }
            });
        }
    }

    public abstract static class CompilerAssertion<T extends Serializable> {
        public abstract void doAssert(Context ctx, JCTree.JCCompilationUnit cu, T args);
    }

    public static <T extends Serializable, A extends CompilerAssertion<T>> void withAssert(Class<A> klass, String code, T args) {
        var output = new StringWriter();

        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new SimpleFileManager(
            compiler.getStandardFileManager(null, null, null));

        final String argsData;
        try (var buffer = new ByteArrayOutputStream()) {
            try (var writer = new ObjectOutputStream(buffer)) {
                writer.writeObject(args);
            }
            argsData = new String(Base64.getEncoder().encode(buffer.toByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        var arguments = asList("--enable-preview", "--source", "17",
            "-classpath", System.getProperty("java.class.path"),
            "-Xplugin:" + AnalyzeAssertionsPlugin.NAME +
                " " + klass.getName() + " " + argsData);

        var task = (BasicJavacTask) compiler.getTask(output, fileManager,
            diagnostic -> System.out.println(diagnostic.toString()),
            arguments, null, List.of(new StringSourceFile("java.lang.A", code)));

        if (!task.call())
            throw new RuntimeException(output.toString());
    }
}
