plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.+"
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}


tasks {
    runServer {
        minecraftVersion("26.3")
    }
}