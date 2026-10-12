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

dependencies {
    implementation(project(":common"))
    implementation(project(":paper"))
    implementation(project(":fabric"))
}

// Paper (1.21.x and 26.x) + Fabric 26.x
tasks.shadowJar {
    archiveClassifier.set("")

    from(project(":common").sourceSets.main.get().output)
    from(project(":paper").sourceSets.main.get().output)
    from(project(":fabric").sourceSets.main.get().output)

    configurations = emptyList()

    mergeServiceFiles()
}

// Paper (1.21.x and 26.x) + Fabric 1.21.x, which is obfuscated and runs on Java 21
val legacyShadowJar by tasks.registering(ShadowJar::class) {
    val legacyRemapJar = project(":fabric-legacy").tasks.named<AbstractArchiveTask>("remapJar")
    dependsOn(legacyRemapJar)

    archiveClassifier.set("mc1.21")

    from(project(":common").sourceSets.main.get().output)
    from(project(":paper").sourceSets.main.get().output)
    from(legacyRemapJar.map { zipTree(it.archiveFile) })

    configurations = emptyList()

    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar, legacyShadowJar)
}

fun ModrinthExtension.configureCommon(gameVersionsProperty: String) {
    token.set(providers.gradleProperty("MODRINTH_TOKEN"))
    projectId.set(property("modrinth_id") as String)
    versionType.set(property("modrinth_version_type") as String)
    loaders.addAll((property("modrinth_loaders") as String).split(","))
    gameVersions.addAll((property(gameVersionsProperty) as String).split(","))
    dependencies {
        required.project("fabric-api")
    }
}

modrinth {
    configureCommon("modrinth_game_versions")
    versionNumber.set(version.toString())
    versionName.set("StandOnTheLetter $version")
    uploadFile.set(tasks.shadowJar)
}

tasks.named("modrinth") {
    dependsOn(tasks.shadowJar)
}

project(":fabric-legacy") {
    apply(plugin = "com.modrinth.minotaur")

    configure<ModrinthExtension> {
        configureCommon("modrinth_legacy_game_versions")
        versionNumber.set("$version+mc1.21")
        versionName.set("StandOnTheLetter $version (MC 1.21.x)")
        uploadFile.set(legacyShadowJar)
    }

    tasks.named("modrinth") {
        dependsOn(legacyShadowJar)
    }
}
