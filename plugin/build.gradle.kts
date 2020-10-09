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
    options.compilerArgs.add("--enable-preview")
    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview")
}

dependencies {
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "greact"
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
                name.set("GReact")
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
