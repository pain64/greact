/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.over64.jscripter.bundler;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;


public class JscripterBundlerPlugin implements Plugin<Project> {

    public static class WorkServerParams implements WorkParameters {
    }

    public abstract static class WebServer implements WorkAction<WorkServerParams> {
        @Override
        public void execute() {
            try {
                System.out.println("AT EXECUTOR");
                _execute();
            } catch (Exception e) {
                System.out.println("RT EXCEPTION: " + e);
                throw new RuntimeException(e);
            }
        }

        void _execute() throws Exception {
            try {
                var client = new WebSocketClient();
                try {
                    client.start();
                    var socket = new ClientHandler();
                    var fut = client.connect(socket, URI.create("ws://localhost:8080/greact_livereload_events/"));
                    Session session = fut.get();

                    StringBuilder message = new StringBuilder("update");
//                    for (class_:changeClasses) {
//                        message.append("**_GREACT_**" + class_.name + "?*_CODE_*&" + class_.code);
//                    }
                    session.getRemote().sendString(message.toString());
                    // session.getRemote().sendString("reload");
                    session.close(org.eclipse.jetty.websocket.api.StatusCode.NORMAL, "I'm done");
                    System.out.println("AFTER SEND");
                } finally {
                    client.stop();
                }
            } catch (java.util.concurrent.ExecutionException t) {
                if (t.getCause() instanceof java.net.ConnectException) {
                    System.out.println("start livereload server!");
                    var server = new Server();
                    var connector = new ServerConnector(server);
                    connector.setPort(8080);
                    server.addConnector(connector);

                    var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                    context.setContextPath("/");
                    server.setHandler(context);

                    JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
                        wsContainer.setMaxTextMessageSize(65535);
                        wsContainer.setIdleTimeout(Duration.ofHours(1));
                        wsContainer.addMapping("/greact_livereload_events/*", ServerHandler.class);
                    });

                    server.start();
                } else throw t;
            } catch (java.io.IOException t) {
                throw new GradleException("Error: cannot use port 8080 for GReact livereload. Is it available?", t);
            }
        }

        public static class ClientHandler implements org.eclipse.jetty.websocket.api.WebSocketListener {
        }

        public static class ServerHandler implements org.eclipse.jetty.websocket.api.WebSocketListener {
            static ConcurrentHashMap.KeySetView<Session, Boolean> sessions = ConcurrentHashMap.newKeySet();
            volatile Session session = null;

            @Override
            public void onWebSocketConnect(Session ss) {
                this.session = ss;
                sessions.add(ss);
            }

            @Override
            public void onWebSocketClose(int statusCode, String reason) {
                var ss = session;
                if (ss != null) sessions.remove(ss);
            }

            @Override
            public void onWebSocketText(String message) {
                System.out.println("####HAS NEW WEBSOCKET MESSAGE: " + message);
                if (!message.equals("reload") && !message.startsWith("update")) return;
                    var me = session;

                    sessions.forEach(ss -> {
                        if (ss != me)
                            try {
                                ss.getRemote().sendString(message);
                            } catch (java.lang.Exception ex) {
                                System.out.println("failed to send livereload message to remote: " + ss.getRemoteAddress());
                            }
                    });

            }
        }
    }

    public static class Livereload extends DefaultTask {
        final WorkerExecutor workerExecutor;

        @Inject
        public Livereload(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        String readJar(JarFile jar, ZipEntry entry) {
            try {
                return new String(jar.getInputStream(entry).readAllBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        String readFile(File file) {
            try {
                return new String(Files.readAllBytes(file.toPath()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        String removeSuffix(String s, String suffix) {
            return s.endsWith(suffix)
                    ? s.substring(0, s.length() - suffix.length())
                    : s;
        }

        record ClassPathWithModule<D>(File classPath, ModuleCode<D> mod) {
        }

        record RResource<D>(String name, D data) {
        }

        record ModuleCode<D>(List<RResource<D>> resources,
                             Map<String, List<String>> dependencies) {
        }


        <E, D> ModuleCode<D> walkOver(Stream<E> stream,
                                      Function<E, String> entryName,
                                      Function<E, String> entryContent,
                                      Function<E, D> entryData) {

            var dependencies = new HashMap<String, List<String>>();
            var resources =
                    stream.filter(e -> {
                                var name = entryName.apply(e);
                                if (name.endsWith(".js") || name.endsWith(".css")) return true;
                                else {
                                    if (name.endsWith(".js.dep")) {
                                        var depData = entryContent.apply(e);
                                        var depName = removeSuffix(name, ".dep");
                                        if (!depData.isEmpty())
                                            dependencies.put(
                                                    depName,
                                                    Arrays.stream(depData.split("\n"))
                                                            .filter(s -> !s.isEmpty())
                                                            .toList());
                                    }
                                    return false;
                                }
                            })
                            .map(e -> new RResource<>(entryName.apply(e), entryData.apply(e)))
                            .toList();

            return new ModuleCode<>(resources, dependencies);
        }

        ModuleCode<String> walkOverJar(File jarFile) {
            try (var jar = new JarFile(jarFile)) {
                return walkOver(jar.stream(),
                        je -> je.getName().replace("/", "."),
                        je -> readJar(jar, je),
                        je -> readJar(jar, je));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        ModuleCode<Path> walkOverDirectory(File baseDir) {
            Function<Path, String> pathToResourceName = path -> {
                var fullName = path.toString();
                if (fullName.endsWith(".js") || fullName.endsWith(".js.dep"))
                    return fullName
                            .replace(baseDir.toPath().toString(), "")
                            .substring(1) // strip /
                            .replace("/", ".");
                else
                    return path.getFileName().toString(); // css
            };

            final Stream<Path> pathsStream;

            try {
                pathsStream = Files.walk(baseDir.toPath()).filter(Files::isRegularFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            return walkOver(
                    pathsStream,
                    pathToResourceName,
                    p -> readFile(p.toFile()),
                    p -> p);
        }

        <D> void pushDependencies2(ModuleCode<D> module,
                                   LinkedHashSet<RResource<D>> dest,
                                   RResource<D> resource) {

            var deps = module.dependencies.getOrDefault(resource.name, List.of());
            for (var dep : deps) {
                // FIXME: try to find dep in library code
                var depResource = module.resources.stream()
                        .filter(r -> r.name.equals(dep))
                        .findFirst();

//                        ?: throw RuntimeException("""
//                        cannot find resource for dependency: $deps
//                        all resources: ${module.resources.joinToString("\n") { it.name }}
//                        """.trimIndent())
                depResource.ifPresent(dr ->
                        pushDependencies2(module, dest, dr));
            }

            dest.add(resource);
        }

        <D> LinkedHashSet<RResource<D>> buildDependencies(ModuleCode<D> module) {
            var dest = new LinkedHashSet<RResource<D>>();
            for (var resource : module.resources)
                pushDependencies2(module, dest, resource);
            return dest;
        }

        @TaskAction
        void reload() {
            var currentTime = System.currentTimeMillis();

            var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
                    ((org.gradle.api.plugins.ExtensionAware) getProject()).getExtensions().getByName("sourceSets");

            var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();
            var stylesDir = resourceDir.toPath().resolve("styles");
            var bundleDir = resourceDir.toPath().resolve("bundle");
//            if(!resourceDir.exists()) {
//                // at source code src/main/resources dir is empty
//                try {
//                    Files.createDirectory(resourceDir.toPath().getParent());
//                    Files.createDirectory(resourceDir.toPath());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }

            try {
                if (bundleDir.resolve("main.js").toFile().exists()) {
                    Files.delete(bundleDir.resolve("main.js"));
                }
                if (bundleDir.resolve("main.css").toFile().exists()) {
                    Files.delete(bundleDir.resolve("main.css"));
                }
                if (bundleDir.resolve(".release").toFile().exists()) {
                    Files.delete(bundleDir.resolve(".release"));
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            if (!bundleDir.toFile().exists())
                try {
                    Files.createDirectory(bundleDir);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            var localJs = sourceSets.getByName("main")
                    .getOutput().getClassesDirs().getFiles().stream()
                    .map(this::walkOverDirectory)
                    .reduce(new ModuleCode<>(List.of(), Map.of()), (m1, m2) ->
                            new ModuleCode<>(
                                    new ArrayList<>(m2.resources) {{
                                        addAll(m2.resources);
                                    }},
                                    new HashMap<>(m1.dependencies) {{
                                        putAll(m2.dependencies);
                                    }}));

            var localCss = walkOverDirectory(stylesDir.toFile()).resources;
            // FIXME: make listConcat & mapConcat method
            var localModule = new ModuleCode<>(
                    new ArrayList<>(localJs.resources) {{
                        addAll(localCss);
                    }},
                    localJs.dependencies);
            var localResourceOrdered = buildDependencies(localModule);

            var runtimeClassPath = getProject().getConfigurations().getByName("runtimeClasspath");
            var libModules = StreamSupport.stream(runtimeClassPath.spliterator(), false)
                    .map(cp -> new ClassPathWithModule<>(cp, walkOverJar(cp)))
                    .filter(cpWithMod -> cpWithMod.mod.resources.stream().anyMatch(r -> r.name.endsWith(".js")))
                    .flatMap(cpWithMod -> {
                        var libResourcesOrdered = buildDependencies(cpWithMod.mod);

                        var libJs = libResourcesOrdered.stream().filter(r -> r.name.endsWith(".js"));
                        var libCss = libResourcesOrdered.stream().filter(r -> r.name.endsWith(".css"));

                        var moduleName = removeSuffix(cpWithMod.classPath.toPath().getFileName().toString(), ".jar");
                        var moduleJsPath = Paths.get(bundleDir + "/" + moduleName + ".js");
                        var moduleCssPath = Paths.get(bundleDir + "/" + moduleName + ".css");

                        try {
                            Files.write(moduleJsPath, String.join("\n", libJs.map(RResource::data).toList()).getBytes());
                            Files.write(moduleCssPath, String.join("\n", libCss.map(RResource::data).toList()).getBytes());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        return Stream.of(new RResource<>(moduleName + ".js", moduleJsPath),
                                new RResource<>(moduleName + ".css", moduleCssPath));
                    }).toList();

            System.out.println("lib js resolution took: " + (System.currentTimeMillis() - currentTime) + "ms");

            var bundleFile = bundleDir.resolve(".bundle");
            try {

                for (var res : localResourceOrdered) {
                    var dest = bundleDir.resolve(res.name);
                    var __ = dest.toFile().mkdirs();
                    try {
                        Files.copy(res.data, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                Files.write(bundleFile, Stream.of(libModules, localResourceOrdered)
                        .flatMap(Collection::stream)
                        .map(r -> {
                            return r.name;
                        })
                        .collect(Collectors.joining("\n"))
                        .getBytes());

                Files.write(bundleFile, Collections.singleton("\nlivereload"), StandardOpenOption.APPEND);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            System.out.println("BEFORE WS MESSAGE SEND! TOOK " + (System.currentTimeMillis() - currentTime) + "ms");

            workerExecutor.noIsolation().submit(WebServer.class, workServerParams -> {
            });
        }
    }

    public static class ProductBuild extends DefaultTask {
        final WorkerExecutor workerExecutor;

        @Inject
        public ProductBuild(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        @TaskAction
        void prod() {
            var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
                    ((org.gradle.api.plugins.ExtensionAware) getProject()).getExtensions().getByName("sourceSets");

            var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();
            var bundleDir = resourceDir.toPath().resolve("bundle");
            var bundleFile = resourceDir.toPath().resolve("bundle").resolve(".bundle");
            List<String> data;

            try {
                data = Files.readAllLines(bundleFile);
                if (!bundleDir.resolve(".release").toFile().exists()) {
                    Files.createFile(bundleDir.resolve(".release"));
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            List<String> css = new ArrayList<>();
            List<String> js = new ArrayList<>();

            for (String file : data) {
                if (file.endsWith("js")) js.add(file);
                if (file.endsWith("css")) css.add(file);
            }

            try {
                var mainJs = new File(bundleDir.resolve("main.js").toString());
                var mainCss = new File(bundleDir.resolve("main.css").toString());

                var __ = mainJs.createNewFile();
                __ = mainCss.createNewFile();

                var jsBuilder = new StringBuilder();
                for (String file : js)
                    jsBuilder.append(Files.readString(bundleDir.resolve(file)));

                Files.writeString(bundleDir.resolve("main.js"), jsBuilder);

                var cssBuilder = new StringBuilder();
                for (String file : css)
                    cssBuilder.append(Files.readString(bundleDir.resolve(file)));

                Files.writeString(bundleDir.resolve("main.css"), cssBuilder);

                for (String file : data)
                    __ = bundleDir.resolve(file).toFile().delete();

                __ = bundleFile.toFile().delete();
                __ = bundleFile.toFile().createNewFile();

                var sha1 = MessageDigest.getInstance("SHA-1");
                var hashJs = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.js"))));
                var hashCss = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.css"))));

                Files.writeString(bundleFile, "main.css " + hashCss + "\nmain.js " + hashJs);
            } catch (IOException | NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void apply(Project project) {
        project.getPlugins().apply("java");
        var classes = project.getTasks().getByName("classes");
        classes.dependsOn("livereload");
        project.getTasks().register("livereload", Livereload.class, reload -> {
            reload.dependsOn("compileJava", "processResources");
        });

        project.getTasks().getByName("jar").dependsOn("prodTask");
        project.getTasks().register("prodTask", ProductBuild.class, reload -> {
            reload.dependsOn("compileJava", "processResources");
        });
    }

    private static String byteArrayToHexString(byte[] b) {
        var result = new StringBuilder();
        for (byte value : b)
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));

        return result.toString();
    }
}
