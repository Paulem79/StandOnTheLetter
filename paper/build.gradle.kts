plugins {
    java
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper") version "3.+"
}

// Built against the oldest supported API so the plugin also runs on 26.x; override with -PpaperApi=<version> to check another one
val paperApi = providers.gradleProperty("paperApi").getOrElse("1.21.1-R0.1-SNAPSHOT")

dependencies {
    implementation(project(":common"))
    implementation("org.bstats:bstats-bukkit:3.2.1")
    compileOnly("io.papermc.paper:paper-api:$paperApi")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to project.property("mod_description")
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveClassifier.set("plain")
    }

    shadowJar {
        archiveBaseName.set("StandOnTheLetter-paper")
        archiveClassifier.set("")

        from(project(":common").sourceSets.main.get().output)

        configurations = project.configurations.runtimeClasspath.map { setOf(it) }

        dependencies {
            // Only merge bStats into the final jar, no other dependency
            exclude { it.moduleGroup != "org.bstats" }
        }

        // Relocated to avoid conflicts with other plugins using bStats
        relocate("org.bstats", "net.paulem.sotl.paper.libs.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.3")
    }
}
