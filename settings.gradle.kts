pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "StandOnTheLetter"

include("fabric")
include("fabric-legacy")
include("paper")
include("common")