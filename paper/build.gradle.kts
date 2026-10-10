plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.+"
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:26.3.rc.+")
}


tasks {
    jar {
        from(project(":common").sourceSets.main.get().output)
    }

    runServer {
        minecraftVersion("26.3")
    }
}