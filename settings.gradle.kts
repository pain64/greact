rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "jscripter:std",
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