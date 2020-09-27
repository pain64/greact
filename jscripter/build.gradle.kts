plugins {
    id("java")
    id("maven-publish")
}

allprojects {
    group = "org.over64"
    repositories {
        jcenter()
    }
}

subprojects {
    version = "0.0.1"
}

tasks.withType<JavaCompile> {
    doFirst {
        println("Args for for $name are ${options.allCompilerArgs}")
        println("classPath:" + classpath.asPath)
        println("sourcePath:" + source.asPath)
        println("resourcePath:" + resources)
        println(options.isFork)
    }

    classpath += files("src/main/resources")
    options.compilerArgs.add("-Xplugin:jScripter " +
            " --js-src-package=org.over64.jscripter.std.java.lang" +
            " --std-conv-class=org.over64.jscripter.std.NoStdTypeConversion")
    options.compilerArgs.add("--enable-preview")
    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview")
}

dependencies {

    implementation("org.apache.commons:commons-math3:3.6.1")
    //implementation(project(":transpiler"))
    implementation(project(":transpiler"))
    implementation("com.google.guava:guava:29.0-jre")
}
