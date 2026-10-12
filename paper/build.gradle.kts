plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.+"
}

// Built against the oldest supported API so the plugin also runs on 26.x; override with -PpaperApi=<version> to check another one
val paperApi = providers.gradleProperty("paperApi").getOrElse("1.21.11-R0.1-SNAPSHOT")

dependencies {
    implementation(project(":common"))
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
        from(project(":common").sourceSets.main.get().output)
    }

    runServer {
        minecraftVersion("26.3")
    }
}
