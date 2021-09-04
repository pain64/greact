plugins {
    id("java")
    id("maven-publish")
}

allprojects {
    group = "com.over64"
    version = "0.0.1"
    repositories {
        jcenter()
    }
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
        " --js-src-package=org.over64.jscripter.std" +
        " --std-conv-class=org.over64.jscripter.NoStdTypeConversion")
    options.compilerArgs.add("--enable-preview")

    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":transpiler"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jscripter"
            groupId = "com.over64"
            from(components["java"])
//            versionMapping {
//                usage("java-api") {
//                    fromResolutionOf("runtimeClasspath")
//                }
//                usage("java-runtime") {
//                    fromResolutionResult()
//                }
//            }
            pom {
                name.set("jScripter")
                description.set("Java to Javascript compiler plugin")
//                url.set("http://www.example.com/library")
//                properties.set(mapOf(
//                        "myProp" to "value",
//                        "prop.with.dots" to "anotherValue"
//                ))
//                licenses {
//                    license {
//                        name.set("The Apache License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("johnd")
//                        name.set("John Doe")
//                        email.set("john.doe@example.com")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:git://example.com/my-library.git")
//                    developerConnection.set("scm:git:ssh://example.com/my-library.git")
//                    url.set("http://example.com/my-library/")
//                }
            }
        }
    }
}
