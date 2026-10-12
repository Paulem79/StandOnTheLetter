plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.18-SNAPSHOT"
    java
}

val minecraftVersion = "1.21.11"

sourceSets.main {
    java.srcDir("../fabric/src/main/java")
    resources.srcDir("../fabric/src/main/resources")
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+$minecraftVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "minecraft" to ">=1.21.6 <26",
        "java" to ">=21"
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
