/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.over64.jscripter.bundler;


import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.over64.jscripter.bundler.CodeAnalyze.*;

public class JScripter implements Plugin<Project> {
    public static class HotReload extends DefaultTask {

        final WorkerExecutor workerExecutor;

        @Inject public HotReload(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }
        @TaskAction void reload() throws IOException {
            var currentTime = System.currentTimeMillis();

            var delta = DebugBuild.debugBuild(getProject());

            var message = "";

            if (delta.isLibrariesChanged() || delta.localFilesChanged().stream().anyMatch(n -> !(n.endsWith(".js") || n.endsWith(".css"))))
                message = "reload";
            else {
                var messageUpdate = new StringBuilder("update");
                for (var file : delta.localFilesChanged())
                    messageUpdate.append("\n").append(file);

                if (!messageUpdate.toString().equals("update"))
                    message = messageUpdate.toString();
            }

            System.out.println("BEFORE WS MESSAGE SEND! TOOK " + (System.currentTimeMillis() - currentTime) + "ms");

            var finalMessage = message;
            workerExecutor.noIsolation().submit(WebsocketSender.WebServer.class, workServerParams -> WebsocketSender.WorkServerParams.message = finalMessage);
        }
    }

    public static class DebugBuild extends DefaultTask {
        final WorkerExecutor workerExecutor;

        @Inject public DebugBuild(WorkerExecutor workerExecutor) {
            this.workerExecutor = workerExecutor;
        }

        record Delta(boolean isLibrariesChanged, List<String> localFilesChanged) { }
        static Delta debugBuild(Project project) throws IOException {
            var currentTime = System.currentTimeMillis();

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

            var latestLibMtime = bundleDir.resolve("latest_lib_mtime");

            if (!Files.exists(latestLibMtime)) {
                Files.createFile(latestLibMtime);
                if (!latestLibMtime.toFile().setLastModified(0))
                    throw new IOException("Can't set lastModified");
            }

            var runtimeClassPath = project.getConfigurations().getByName("runtimeClasspath");
            var opt = StreamSupport.stream(runtimeClassPath.spliterator(), false).map(File::lastModified).max(Long::compare);
            long maxJarMtime = opt.isPresent() ? opt.get() : -1;

            if (maxJarMtime < 0) throw new RuntimeException("Can't read jar files");

            var classPathIsChanged = maxJarMtime > latestLibMtime.toFile().lastModified();

            if (classPathIsChanged) {
                if (!latestLibMtime.toFile().setLastModified(maxJarMtime))
                    throw new IOException("Can't set lastModified");

                var libs = fetchLibrariesCode(project);

                var moduleJsPath = Paths.get(bundleDir + "/lib.js");
                var moduleCssPath = Paths.get(bundleDir + "/lib.css");

                Files.writeString(moduleJsPath, libs.js());
                Files.writeString(moduleCssPath, libs.css());
            }

            var localResourceOrdered = fetchLocalCode(project);

            System.out.println("lib js resolution took: " + (System.currentTimeMillis() - currentTime) + "ms");


            var bundlePath = bundleDir.resolve(".bundle");
            var bundleFile = bundlePath.toFile();
            var bundleExists = bundleFile.exists();
            var lastBuild = bundleFile.lastModified();

            var changedFiles_ = new ArrayList<String>();

            for (var res : localResourceOrdered) { // TODO: Loader
                if (!bundleExists || res.data().toFile().lastModified() > lastBuild) {
                    changedFiles_.add(res.name());
                    var dest = bundleDir.resolve(res.name());

                    if (bundleExists && res.name().endsWith(".js")) {
                        var adopted = replaceClassDeclarationWithWindow(Files.readString(res.data()));
                        var destFile = dest.toFile();
                        if (!destFile.exists()) { var __ = destFile.createNewFile(); }
                        Files.writeString(dest, adopted, StandardOpenOption.TRUNCATE_EXISTING);
                    } else
                        Files.copy(res.data(), dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            Files.writeString(bundlePath, "lib.js\nlib.css\n");
            Files.writeString(bundlePath, Stream.of(localResourceOrdered)
                .flatMap(Collection::stream)
                .map(RResource::name)
                .collect(Collectors.joining("\n")), StandardOpenOption.APPEND);
            Files.writeString(bundlePath, "\nlivereload", StandardOpenOption.APPEND);

            return new Delta(classPathIsChanged, changedFiles_);
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

        interface Appender {
            void appendAndDrop(FileOutputStream to, String fileName) throws IOException;
        }

        @TaskAction
        void productionBuild() throws IOException, NoSuchAlgorithmException {
            var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
                ((org.gradle.api.plugins.ExtensionAware) getProject()).getExtensions().getByName("sourceSets");

            var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();
            assert resourceDir != null;
            var bundleDir = resourceDir.toPath().resolve("bundle");

            if (bundleDir.toFile().exists())
                CodeAnalyze.deleteDir(bundleDir.toFile());

            Files.createDirectory(bundleDir);

            var bundleFile = bundleDir.resolve(".bundle");
            Files.createFile(bundleFile);

            var libs = fetchLibrariesCode(getProject());

            var moduleJsPath = Paths.get(bundleDir + "/lib.js");
            var moduleCssPath = Paths.get(bundleDir + "/lib.css");

            Files.writeString(moduleJsPath, libs.js());
            Files.writeString(moduleCssPath, libs.css());

            var mainJsFile = bundleDir.resolve("main.js").toFile();
            var mainCssFile = bundleDir.resolve("main.css").toFile();

            if (!mainJsFile.exists()) { var __ = mainJsFile.createNewFile(); }
            if (!mainCssFile.exists()) { var __ = mainCssFile.createNewFile(); }

            var allFileNames = fetchLocalCode(getProject()).stream().map(CodeAnalyze.RResource::data).toList();

            Appender appender = (to, fileName) -> {
                var inFile = new File(fileName);
                try (var in = new FileInputStream(inFile)) {
                    var inCh = in.getChannel();
                    inCh.transferTo(0, inCh.size(), to.getChannel());
                }
                var __ = inFile.delete();
            };

            try (var outJs = new FileOutputStream(mainJsFile, false);
                 var outCss = new FileOutputStream(mainCssFile, false)) {
                for (var fileName : allFileNames) {
                    var fileName_ = fileName.toFile().getAbsolutePath();
                    System.out.println(fileName_);
                    if (fileName_.endsWith(".js"))
                        appender.appendAndDrop(outJs, fileName_);
                    else if (fileName_.endsWith(".css"))
                        appender.appendAndDrop(outCss, fileName_);
                }
            }

            var __ = bundleFile.toFile().delete();
            __ = bundleFile.toFile().createNewFile();

            var sha1 = MessageDigest.getInstance("SHA-1");

            var hashJs = CodeAnalyze.byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.js"))));
            var hashCss = CodeAnalyze.byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("main.css"))));
            var hashLibJs = CodeAnalyze.byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("lib.js"))));
            var hashLibCss = CodeAnalyze.byteArrayToHexString(sha1.digest(Files.readAllBytes(bundleDir.resolve("lib.css"))));

            Files.writeString(bundleFile,
                "lib.js " + hashLibJs +
                    "\nlib.css " + hashLibCss +
                    "\nmain.css " + hashCss +
                    "\nmain.js " + hashJs);
        }
    }
    //  TODO: используем NIO (zero-copy) для работы с файлами (426-431, 366, 369)

    public void apply(Project project) {
        project.getPlugins().apply("java");
        var classes = project.getTasks().getByName("classes");

        project.getTasks().register("bundler-debug-build", DebugBuild.class, debug -> debug.dependsOn("compileJava", "processResources"));
        classes.dependsOn("bundler-debug-build");

        project.getTasks().register("hot-reload", HotReload.class, reload ->
            reload.dependsOn("compileJava", "processResources"));

        project.getTasks().register("bundler-production-build", ProductBuild.class, productBuild -> productBuild.dependsOn("compileJava", "processResources"));
        project.getTasks().getByName("jar").dependsOn("bundler-production-build");
    }
}
