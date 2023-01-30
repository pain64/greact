rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "jscripter:std",
    "greact",
    "greact-uikit",
    "demo",
    "greact-uikit-docs",
    "ssql"
)

pluginManagement {
    includeBuild("jscripter/bundler")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}