plugins {
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT"
    java
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:26.3")
    compileOnly("net.fabricmc:fabric-loader:0.16.9")
    compileOnly("net.fabricmc.fabric-api:fabric-api:0.161.0+26.3")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}