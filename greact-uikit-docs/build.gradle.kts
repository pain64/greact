plugins {
    id("java")
    id("jstack.jscripter.bundler") version "0.0.1"
}

allprojects {
    group = "jstack"
    version = "0.0.1"
    repositories {
        jcenter()
        mavenLocal()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    options.compilerArgs.add("--enable-preview")
    options.compilerArgs.add("-Xplugin:GReact --js-src-package=jstack.greact.uikit.docs.js")
    options.fork()
    options.forkOptions.jvmArgs = listOf(
        "--enable-preview",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED"
    )
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
    jvmArgs = listOf(
        "--enable-preview",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":jscripter:transpiler"))
    implementation(project(":jscripter:std"))
    implementation(project(":greact"))
    implementation(project(":greact-uikit"))
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("com.sparkjava:spark-core:2.9.2")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("commons-io:commons-io:2.10.0")

    implementation("org.postgresql:postgresql:42.3.6")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "jstack.greact.uikit.docs.Main"
    }

//    FIXME: problem with Gradle
//    duplicatesStrategy = DuplicatesStrategy.WARN
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
