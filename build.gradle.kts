plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("maven-publish")
    id("signing")
}

allprojects {
    group = "com.groq.api"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("org.jetbrains.dokka")
        plugin("maven-publish")
        plugin("signing")
    }
}