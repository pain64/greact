package jstack.jscripter.bundler;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

public class CodeAnalyze {

    static String readJar(JarFile jar, ZipEntry entry) {
        try {
            return new String(jar.getInputStream(entry).readAllBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static String readFile(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static String removeDepSuffix(String s) {
        return s.endsWith(".dep")
            ? s.substring(0, s.length() - ".dep".length())
            : s;
    }

    record ClassPathWithModule<D>(File classPath, ModuleCode<D> mod) { }
    record RResource<D>(String name, D data) { }
    record ModuleCode<D>(List<RResource<D>> resources,
                         Map<String, List<String>> dependencies) { }

    static <E, D> ModuleCode<D> walkOver(Stream<E> stream,
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
                            var depName = removeDepSuffix(name);
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

    static ModuleCode<String> walkOverJar(File jarFile) {
        try (var jar = new JarFile(jarFile)) {
            return walkOver(jar.stream(),
                je -> je.getName().replace("/", "."),
                je -> readJar(jar, je),
                je -> readJar(jar, je));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static ModuleCode<Path> walkOverDirectory(File baseDir) {
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

    static <D> void pushDependencies2(ModuleCode<D> module,
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

    static <D> LinkedHashSet<RResource<D>> buildDependencies(ModuleCode<D> module) {
        var dest = new LinkedHashSet<RResource<D>>();
        for (var resource : module.resources)
            pushDependencies2(module, dest, resource);
        return dest;
    }

    static boolean isModADependsOnModB(
        CodeAnalyze.ClassPathWithModule<String> mA,
        CodeAnalyze.ClassPathWithModule<String> mB
    ) {
        return mA.mod.dependencies.values().stream().anyMatch(dependencies ->
            dependencies.stream().anyMatch(dependency ->
                mB.mod.resources.stream().anyMatch(r -> r.name.equals(dependency))
            )
        );
    }
    static LibrariesCode fetchLibrariesCode(Project project) {
        var currentTime = System.currentTimeMillis();

        var js = new StringBuilder();
        var css = new StringBuilder();

        var runtimeClassPath = project.getConfigurations().getByName("runtimeClasspath");
        StreamSupport.stream(runtimeClassPath.spliterator(), false)
            .map(cp -> new ClassPathWithModule<>(cp, walkOverJar(cp)))
            .filter(cpWithMod -> cpWithMod.mod.resources.stream().anyMatch(r -> r.name.endsWith(".js")))
            .sorted((m1, m2) -> {
                if (isModADependsOnModB(m1, m2)) {
                    return isModADependsOnModB(m2, m1) ? 0 : 1;
                } else return -1;
            })
            .forEach(cpWithMod -> {
                var libResourcesOrdered = buildDependencies(cpWithMod.mod);

                var libJs = libResourcesOrdered.stream().filter(r -> r.name.endsWith(".js"));
                var libCss = libResourcesOrdered.stream().filter(r -> r.name.endsWith(".css"));

                js.append(String.join("\n", libJs.map(RResource::data).toList()));
                css.append(String.join("\n", libCss.map(RResource::data).toList()));
            });

        System.out.println("lib js resolution took: " +
            (System.currentTimeMillis() - currentTime) + "ms");

        return new LibrariesCode(js.toString(), css.toString());
    }

    static LinkedHashSet<RResource<Path>> fetchLocalCode(Project project) {
        var sourceSets = (org.gradle.api.tasks.SourceSetContainer)
            ((org.gradle.api.plugins.ExtensionAware) project).getExtensions().getByName("sourceSets");

        var resourceDir = sourceSets.getByName("main").getOutput().getResourcesDir();

        assert resourceDir != null;

        var localJs = sourceSets.getByName("main")
            .getOutput().getClassesDirs().getFiles().stream()
            .map(CodeAnalyze::walkOverDirectory)
            .reduce(new ModuleCode<>(List.of(), Map.of()), (m1, m2) ->
                new ModuleCode<>(
                    new ArrayList<>(m2.resources) {{ addAll(m2.resources); }},
                    new HashMap<>(m1.dependencies) {{ putAll(m2.dependencies); }}));

        var externalJsDir = resourceDir.toPath().resolve("js");
        var externalJs = externalJsDir.toFile().exists()
            ? walkOverDirectory(externalJsDir.toFile()).resources
            : new ArrayList<RResource<Path>>();

        var stylesDir = resourceDir.toPath().resolve("styles");
        var localCss = stylesDir.toFile().exists()
            ? walkOverDirectory(stylesDir.toFile()).resources
            : new ArrayList<RResource<Path>>();

        // FIXME: make listConcat & mapConcat method
        var localModule = new ModuleCode<>(
            new ArrayList<>(localJs.resources) {{
                addAll(localCss);
                addAll(externalJs);
            }},
            localJs.dependencies);

        return buildDependencies(localModule);
    }

    record LibrariesCode(String js, String css) { }
    static CharSequence replaceClassDeclarationWithWindow(String readString) {
        return readString.replaceAll("class (\\S*)( extends (\\S*))? \\{", "window.$1 = class$2 {");
    }
}
