pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "StandOnTheLetter"

include("fabric")
include("paper")
include("common")