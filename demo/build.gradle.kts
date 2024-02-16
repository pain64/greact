plugins {
    id("java")
    id("org.flywaydb.flyway") version "9.8.1"
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

val devDbName = "jstack_demo"
val devDbUser = "jstack"
val devDbPassword = "1234"

val compileTimeSqlCheckEnabled = file("compile_time_sql_checks_enabled").exists()

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    options.compilerArgs.add("--enable-preview")
    options.compilerArgs.add(
        "-Xplugin:GReact --js-src-package=jstack.demo.js " +
//                "--rpc-url=/some_rpc_url " +
                if (compileTimeSqlCheckEnabled) {
                    "--tsql-check-enabled=true " +
                            "--tsql-check-driver-classname=org.postgresql.Driver " +
                            "--tsql-check-schema-url=jdbc:postgresql://localhost:5432/$devDbName " +
                            "--tsql-check-schema-username=$devDbUser " +
                            "--tsql-check-schema-password=$devDbPassword " +
                            "--tsql-check-dialect-classname=jstack.demo.Dialect"
                } else ""
    )
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":jscripter:transpiler"))
    implementation(project(":jscripter:std"))
    implementation(project(":ssql"))
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
        attributes["Main-Class"] = "jstack.demo.Main"
    }

//    FIXME: problem with Gradle
//    duplicatesStrategy = DuplicatesStrategy.WARN
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

flyway {
    driver = "org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:5432/$devDbName"
    user = devDbUser
    password = devDbPassword
    cleanDisabled = false
}
