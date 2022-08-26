/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.over64.jscripter.bundler;


import com.over64.jscripter.bundler.CodeAnalyze.RResource;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JScripterBundlerPlugin implements Plugin<Project> {
    public static class HotReload extends DefaultTask {

        final WorkerExecutor workerExecutor;

        @Inject public HotReload(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        @TaskAction void reload() throws IOException {
            var currentTime = System.currentTimeMillis();

            var delta = DebugBuild.debugBuild(getProject());
            final String message;

            if (delta.isLibrariesChanged ||
                delta.localFilesChanged().stream().anyMatch(
                    n -> !n.endsWith(".js") && !n.endsWith(".css")))
                message = "reload";
            else {
                var messageUpdate = new StringBuilder("update");
                for (var file : delta.localFilesChanged)
                    messageUpdate.append("\n").append(file);

                message = messageUpdate.toString();
            }

            System.out.println("BEFORE WS MESSAGE SEND! TOOK " +
                (System.currentTimeMillis() - currentTime) + "ms");

            workerExecutor.noIsolation().submit(WebsocketSender.WebServer.class,
                workServerParams -> workServerParams.message = message);
        }
    }

    public static class DebugBuild extends DefaultTask {
        final WorkerExecutor workerExecutor;

        @Inject public DebugBuild(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        record Delta(boolean isLibrariesChanged, List<String> localFilesChanged) { }
        static Delta debugBuild(Project project) throws IOException {
            var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
                ((org.gradle.api.plugins.ExtensionAware) project).getExtensions().getByName("sourceSets");

            var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();

            assert resourceDir != null;
            var bundleDir = resourceDir.toPath().resolve("bundle");
            if (!bundleDir.toFile().exists()) Files.createDirectory(bundleDir);

            if (Files.exists(bundleDir.resolve("main.js")))
                Files.delete(bundleDir.resolve("main.js"));
            if (Files.exists(bundleDir.resolve("main.css")))
                Files.delete(bundleDir.resolve("main.css"));

            var runtimeClassPath = project.getConfigurations().getByName("runtimeClasspath");

            long maxJarMtime = StreamSupport
                .stream(runtimeClassPath.spliterator(), false)
                .map(File::lastModified).max(Long::compare).orElse(0L);

            var libMtimePath = bundleDir.resolve("latest_lib_mtime");
            final long libMtime;

            if (!Files.exists(libMtimePath)) {
                Files.createFile(libMtimePath);
                libMtime = 0L;
            } else
                libMtime = libMtimePath.toFile().lastModified();

            var isClassPathChanged = maxJarMtime > libMtime;

            if (isClassPathChanged) {
                if (!libMtimePath.toFile().setLastModified(maxJarMtime))
                    throw new IOException("Can't set lastModified");

                var libs = CodeAnalyze.fetchLibrariesCode(project);

                var moduleJsPath = Paths.get(bundleDir + "/lib.js");
                var moduleCssPath = Paths.get(bundleDir + "/lib.css");

                Files.writeString(moduleJsPath, libs.js());
                Files.writeString(moduleCssPath, libs.css());
            }

            var localResourceOrdered = CodeAnalyze.fetchLocalCode(project);

            var bundlePath = bundleDir.resolve(".bundle");
            var bundleFile = bundlePath.toFile();
            var bundleExists = bundleFile.exists();
            var lastBuild = bundleFile.lastModified();

            var changedFiles = new ArrayList<String>();

            for (var res : localResourceOrdered) {
                if (!bundleExists || res.data().toFile().lastModified() > lastBuild) {
                    changedFiles.add(res.name());
                    var dest = bundleDir.resolve(res.name());

                    if (res.name().endsWith(".js")) {
                        var adopted = CodeAnalyze
                            .replaceClassDeclarationWithWindow(Files.readString(res.data()));
                        var destFile = dest.toFile();
                        if (!destFile.exists()) { var __ = destFile.createNewFile(); }
                        Files.writeString(dest, adopted, StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        try (var out = new FileOutputStream(dest.toFile(), false)) {
                            append(out, res.data().toFile().getAbsolutePath());
                        }
                    }
                }
            }

            Files.writeString(bundlePath, "lib.js\nlib.css\n");
            Files.writeString(bundlePath, Stream.of(localResourceOrdered)
                .flatMap(Collection::stream)
                .map(RResource::name)
                .collect(Collectors.joining("\n")), StandardOpenOption.APPEND);
            Files.writeString(bundlePath, "\nhot-reload", StandardOpenOption.APPEND);

            return new Delta(isClassPathChanged, changedFiles);
        }

        @TaskAction void debugBuild_() throws Exception {
            debugBuild(getProject());
        }
    }

    public static class ProductBuild extends DefaultTask {
        final WorkerExecutor workerExecutor;

        @Inject
        public ProductBuild(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        @TaskAction
        void productionBuild() throws IOException, NoSuchAlgorithmException {
            var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
                ((org.gradle.api.plugins.ExtensionAware) getProject()).getExtensions().getByName("sourceSets");

            var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();
            assert resourceDir != null;
            var bundleDir = resourceDir.toPath().resolve("bundle");

            if (bundleDir.toFile().exists())
                deleteDir(bundleDir.toFile());

            Files.createDirectory(bundleDir);

            var bundleFile = bundleDir.resolve(".bundle");
            Files.createFile(bundleFile);

            var libs = CodeAnalyze.fetchLibrariesCode(getProject());

            var moduleJsPath = Paths.get(bundleDir + "/lib.js");
            var moduleCssPath = Paths.get(bundleDir + "/lib.css");

            Files.writeString(moduleJsPath, libs.js());
            Files.writeString(moduleCssPath, libs.css());

            var mainJsFile = bundleDir.resolve("main.js").toFile();
            var mainCssFile = bundleDir.resolve("main.css").toFile();

            if (!mainJsFile.exists()) { var __ = mainJsFile.createNewFile(); }
            if (!mainCssFile.exists()) { var __ = mainCssFile.createNewFile(); }

            var allFileNames = CodeAnalyze.fetchLocalCode(
                getProject()).stream().map(RResource::data).toList();

            try (var outJs = new FileOutputStream(mainJsFile, false);
                 var outCss = new FileOutputStream(mainCssFile, false)) {
                for (var fileName : allFileNames) {
                    var filePath = fileName.toFile().getAbsolutePath();
                    if (filePath.endsWith(".js"))
                        appendAndDrop(outJs, filePath);
                    else if (filePath.endsWith(".css"))
                        appendAndDrop(outCss, filePath);
                }
            }

            var __ = bundleFile.toFile().delete();
            __ = bundleFile.toFile().createNewFile();

            var sha1 = MessageDigest.getInstance("SHA-1");

            var hashJs = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.js"))));
            var hashCss = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.css"))));
            var hashLibJs = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("lib.js"))));
            var hashLibCss = byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("lib.css"))));

            Files.writeString(bundleFile,
                "lib.js " + hashLibJs +
                    "\nlib.css " + hashLibCss +
                    "\nmain.css " + hashCss +
                    "\nmain.js " + hashJs);
        }
    }

    public void apply(Project project) {
        project.getPlugins().apply("java");

        project.getTasks().register("bundlerDebugBuild", DebugBuild.class, debugBuild -> {
            debugBuild.dependsOn("compileJava", "processResources");
        });
        project.getTasks().getByName("classes").dependsOn("bundlerDebugBuild");
        project.getTasks().register("hotReload", HotReload.class,
            reload -> reload.dependsOn("compileJava", "processResources"));

        project.getTasks().register("bundlerProductionBuild", ProductBuild.class,
            productBuild -> productBuild.dependsOn("classes"));
        project.getTasks().getByName("jar").dependsOn("bundlerProductionBuild");
    }

    static String byteArrayToHexString(byte[] b) {
        var result = new StringBuilder();
        for (byte value : b)
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));

        return result.toString();
    }

    static void deleteDir(File file) {
        var contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        var __ = file.delete();
    }
    static public void appendAndDrop(FileOutputStream to, String fileName) throws IOException {
        var inFile = new File(fileName);
        append(to, fileName);
        var __ = inFile.delete();
    }
    static public void append(FileOutputStream to, String fileName) throws IOException {
        var inFile = new File(fileName);
        try (var in = new FileInputStream(inFile)) {
            var inCh = in.getChannel();
            inCh.transferTo(0, inCh.size(), to.getChannel());
        }
    }
}