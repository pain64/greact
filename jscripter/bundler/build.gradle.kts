/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Gradle plugin project to get you started.
 * For more details take a look at the Writing Custom Plugins chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.2/userguide/custom_plugins.html
 */

allprojects {
    group = "jstack"
    version = "0.0.1"
}

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    id("maven-publish")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    implementation("org.eclipse.jetty.websocket:websocket-jetty-api:10.0.2")
    implementation("org.eclipse.jetty.websocket:websocket-jetty-server:10.0.2")
    implementation("org.eclipse.jetty.websocket:websocket-jetty-client:10.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

gradlePlugin {
    // Define the plugin
    val greeting by plugins.creating {
        id = "jstack.jscripter.bundler"
        implementationClass = "jstack.jscripter.bundler.JScripterBundlerPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jscripter-bundler"
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
                description.set("JScripter bundler gradle plugin")
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
