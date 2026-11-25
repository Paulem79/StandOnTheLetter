plugins {
    id("net.fabricmc.fabric-loom") version "1.19.4"
    java
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.19.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.167.1+26.4")
    implementation("maven.modrinth:midnightlib:1.10.0+26.3-fabric")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "description" to project.property("mod_description"),
        "minecraft" to ">=26.1",
        "java" to ">=25"
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
