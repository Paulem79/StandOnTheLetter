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

tasks.shadowJar {
    archiveClassifier.set("")

    from(project(":common").sourceSets.main.get().output)
    from(project(":paper").sourceSets.main.get().output)
    from(project(":fabric").sourceSets.main.get().output)

    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}