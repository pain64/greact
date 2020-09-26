plugins {
    id("java-library")
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

dependencies {
    api("org.apache.commons:commons-math3:3.6.1")
    implementation(project(":transpiler"))
    implementation("com.google.guava:guava:29.0-jre")
}
