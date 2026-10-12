import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("com.gradleup.shadow") version "9.+"
}

allprojects {
    group = "net.paulem.sotl"
    version = "1.0.0"

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
