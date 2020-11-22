plugins {
    id("java")
}

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.sparkjava:spark-core:2.9.2")
    implementation("com.over64:jscripter:0.0.1")
    implementation("com.over64:jscripter-transpiler:0.0.1")
    implementation("com.over64:greact:0.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
//    options.compilerArgs.add("-Xplugin:jScripter --js-src-package=greact.sample.plainjs")
    options.compilerArgs.add("-Xplugin:GReact --js-src-package=greact.sample.plainjs")
    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview")
}