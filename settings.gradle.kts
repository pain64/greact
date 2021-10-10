rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "jscripter:bundler",
    "greact",
    "greact-uikit",
    "greact-uikit-sample",
    "typesafesql"
)

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}