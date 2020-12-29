plugins {
    id("java")
}

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.sparkjava:spark-core:2.9.2")
    implementation("org.sql2o:sql2o:1.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("com.over64:jscripter:0.0.1")
    // FIXME: эта зависимость должна приехать автоматически???
    implementation("com.over64:jscripter-transpiler:0.0.1")
    implementation("com.over64:greact:0.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
//    options.compilerArgs.add("-Xplugin:jScripter --js-src-package=greact.sample.plainjs")
    options.compilerArgs.add("-Xplugin:GReact --js-src-package=greact.sample.plainjs")
    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview")
}