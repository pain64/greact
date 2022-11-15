rootProject.name = "jstack"
include(
    "jscripter:transpiler",
    "jscripter:std",
    "greact",
    "greact-uikit",
    "greact-uikit-sample:demo",
    "greact-uikit-sample:greact-uikit-docs",
    "tsql"
)

pluginManagement {
    includeBuild("jscripter/bundler")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
include("greact-uikit-sample:demo")
findProject(":greact-uikit-sample:demo")?.name = "demo"
