pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

if (file("Ponder").exists()) {
    includeBuild(".")
    includeBuild("Ponder") {
        dependencySubstitution {
			substitute(module("net.createmod.ponder:ponder-fabric")).using(project(":fabric"))
			substitute(module("net.createmod.ponder:ponder-common")).using(project(":common"))
        }
    }
}
