rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "jscripter:std",
    "greact:greact",
    "greact:greact-uikit",
    "greact:greact-uikit-sample:demo",
    "greact:greact-uikit-sample:greact-uikit-docs",
    "tsql"
)

pluginManagement {
    includeBuild("jscripter/bundler")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}