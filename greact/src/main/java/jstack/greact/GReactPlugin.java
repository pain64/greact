package jstack.greact;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.URLClassPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;


public class GReactPlugin implements Plugin {
    public static final String NAME = "GReact";

    @Override public String getName() {
        return NAME;
    }

    static ClassLoader findBuiltinClassLoader(ClassLoader entry) {
        return entry instanceof BuiltinClassLoader
            ? entry : findBuiltinClassLoader(entry.getParent());
    }

    // 0. Починить утекающий classpath
    //   - через раскаковку jar и подсовывание ровно одного class файла
    // 1. By classpath +
    //  1.1. concurrency
    //    - загружаем только класс, new instance делаем каждый раз заново
    //  1.2 connection pool
    //    -
    // 2. By plugin args

    static Pair<Map<String, Pair<Long, Class<?>>>, Context.Key<Object>> loadHolderClasses(
        ClassLoader contextCL
    ) throws Exception {
        var classHolderClass = contextCL.loadClass("jstack.greact.ClassHolder");
        var classesField = classHolderClass.getField("classes");
        var instanceKeyField = classHolderClass.getField("greactInstanceKey");

        //noinspection unchecked
        return new Pair<>(
            (Map<String, Pair<Long, Class<?>>>) classesField.get(null),
            (Context.Key<Object>) instanceKeyField.get(null)
        );
    }

    public static Pair<Class<?>, Context.Key<Object>> cachedClass() throws Exception {
        var currentCL = (URLClassLoader) GReactPlugin.class.getClassLoader();
        var contextCL = findBuiltinClassLoader(currentCL);

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (contextCL) {
            Pair<Map<String, Pair<Long, Class<?>>>, Context.Key<Object>> holder;

            try {
                holder = loadHolderClasses(contextCL);
            } catch (ClassNotFoundException ex) {
                System.out.println("LOAD GREACT CLASSES CACHE");

                var ucpField = BuiltinClassLoader.class.getDeclaredField("ucp");
                ucpField.setAccessible(true);
                var ucp = (URLClassPath) ucpField.get(contextCL);

                for (var url : currentCL.getURLs()) {
                    //System.out.println("URL = " + url);
                    if (url.getFile().matches(".*greact-\\d+\\.\\d+\\.\\d+\\.jar$")) {
                        ucp.addURL(new URL("x-buffer", null, -1, "/", new URLStreamHandler() {
                            final JarFile jar = new JarFile(new File(url.getFile()));

                            protected URLConnection openConnection(URL u) throws IOException {
                                // System.out.println("HERE = " + u.getFile());
                                if (!u.getFile().equals("/jstack/greact/ClassHolder.class"))
                                    throw new FileNotFoundException(u.getFile());

//                                System.out.println("HERE2");
//
//                                var enu = jar.entries();
//                                while(enu.hasMoreElements()) {
//                                    var je = enu.nextElement();
//                                    System.out.println(je.getName());
//                                }

                                var je = jar.getEntry(u.getFile().replaceFirst("/", ""));

                                // System.out.println("HERE3 " + je);

                                return new URLConnection(u) {
                                    @Override public void connect() { }
                                    @Override
                                    public InputStream getInputStream() throws IOException {
                                        return jar.getInputStream(je);
                                    }
                                };
                            }
                        }));
                    }
                }

                holder = loadHolderClasses(contextCL);
            }

            var maxMtime = 0L;
            var keyBuilder = new StringBuilder();
            for (var url : currentCL.getURLs())
                if (url.getProtocol().equals("file")) {
                    keyBuilder.append(url.getFile()).append("\n");
                    var mtime = new File(url.getFile()).lastModified();
                    if (maxMtime < mtime) maxMtime = mtime;
                }

            var key = keyBuilder.toString();
            var loadedClass = holder.fst.get(key);

            final Class<?> _class;
            if (loadedClass == null || loadedClass.fst < maxMtime) {
                if (loadedClass != null) {
                    var closeMethod = loadedClass.snd.getMethod("close");
                    closeMethod.invoke(null);
                }

                System.out.println("LOAD GREACT CLASSES FOR CLASSPATH:\n" + key);

                @SuppressWarnings("resource")
                var myUrlClassLoader = new URLClassLoader(currentCL.getURLs());
                _class = myUrlClassLoader.loadClass("jstack.greact.GReactPluginW");
                holder.fst.put(key, new Pair<>(maxMtime, _class));
            } else
                _class = loadedClass.snd;

            return new Pair<>(_class, holder.snd);
        }
    }


    @Override public void init(JavacTask task, String... strings) {
        // We are running with JUnit
        if (GReactPlugin.class.getClassLoader() instanceof BuiltinClassLoader) {
            new GReactPluginW().init(task, strings);
            return;
        }

        try {
            var context = ((BasicJavacTask) task).getContext();
            var classAndKey = cachedClass();
            var instance = cachedClass().fst.newInstance();
            context.put(classAndKey.snd, instance);

            var method = instance.getClass().getMethod("init", JavacTask.class, String[].class);
            method.invoke(instance, task, strings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}