rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "greact",
    "greact-uikit",
    "greact-uikit-sample",
    "typesafesql"
)

pluginManagement {
    includeBuild("jscripter/bundler")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}