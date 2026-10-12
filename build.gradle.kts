import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.modrinth.minotaur.ModrinthExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("com.gradleup.shadow") version "9.+"
    id("com.modrinth.minotaur") version "2.+"
}

allprojects {
    group = "net.paulem.sotl"
    version = property("mod_version") as String

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
}

// Fabric 26.x
tasks.shadowJar {
    archiveClassifier.set("fabric")

    from(project(":common").sourceSets.main.get().output)
    from(project(":fabric").sourceSets.main.get().output)

    configurations = emptyList()

    mergeServiceFiles()
}

// Fabric 1.21.x, which is obfuscated and runs on Java 21
val legacyShadowJar by tasks.registering(ShadowJar::class) {
    val legacyRemapJar = project(":fabric-legacy").tasks.named<AbstractArchiveTask>("remapJar")
    dependsOn(legacyRemapJar)

    archiveClassifier.set("fabric-mc1.21")

    from(project(":common").sourceSets.main.get().output)
    from(legacyRemapJar.map { zipTree(it.archiveFile) })

    configurations = emptyList()

    mergeServiceFiles()
}

// Paper plugin (Paper, Purpur, Folia)
// Looked up lazily: the paper project is configured after this one
val paperJar = provider { project(":paper").tasks.named<AbstractArchiveTask>("shadowJar").get() }

tasks.build {
    dependsOn(tasks.shadowJar, legacyShadowJar, paperJar)
}

fun ModrinthExtension.configureCommon(loadersProperty: String, gameVersions: List<String>, requireFabricApi: Boolean) {
    token.set(providers.gradleProperty("MODRINTH_TOKEN"))
    projectId.set(property("modrinth_id") as String)
    versionType.set(property("modrinth_version_type") as String)
    loaders.addAll((property(loadersProperty) as String).split(","))
    this.gameVersions.addAll(gameVersions)
    if (requireFabricApi) {
        dependencies {
            required.project("fabric-api")
        }
    }
}

fun versionsOf(property: String) = (property(property) as String).split(",")

modrinth {
    configureCommon("modrinth_fabric_loaders", versionsOf("modrinth_game_versions"), true)
    versionNumber.set("$version+fabric")
    versionName.set("StandOnTheLetter $version (Fabric)")
    uploadFile.set(tasks.shadowJar)
}

tasks.named("modrinth") {
    dependsOn(tasks.shadowJar)
}

project(":fabric-legacy") {
    apply(plugin = "com.modrinth.minotaur")

    configure<ModrinthExtension> {
        configureCommon("modrinth_fabric_loaders", versionsOf("modrinth_legacy_game_versions"), true)
        versionNumber.set("$version+fabric.mc1.21")
        versionName.set("StandOnTheLetter $version (Fabric, MC 1.21.x)")
        uploadFile.set(legacyShadowJar)
    }

    tasks.named("modrinth") {
        dependsOn(legacyShadowJar)
    }
}

project(":paper") {
    apply(plugin = "com.modrinth.minotaur")

    configure<ModrinthExtension> {
        configureCommon(
            "modrinth_paper_loaders",
            versionsOf("modrinth_paper_extra_game_versions") + versionsOf("modrinth_legacy_game_versions") + versionsOf("modrinth_game_versions"),
            false
        )
        versionNumber.set("$version+paper")
        versionName.set("StandOnTheLetter $version (Paper)")
        uploadFile.set(paperJar)
    }

    tasks.named("modrinth") {
        dependsOn(paperJar)
    }
}
