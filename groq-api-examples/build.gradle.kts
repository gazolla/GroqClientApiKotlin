plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("com.groq.examples.Main")
}

dependencies {
    implementation(project(":groq-api-core"))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // CLI utilities (optional for examples)
    implementation("com.github.ajalt.clikt:clikt:4.2.2")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

tasks.shadowJar {
    archiveBaseName.set("groq-api-examples")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}