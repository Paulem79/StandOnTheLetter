plugins {
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT"
    java
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.19.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.162.0+26.3")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "minecraft" to ">=26.1",
        "java" to ">=25"
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
