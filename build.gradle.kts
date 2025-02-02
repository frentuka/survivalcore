import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("java")
    id("com.gradle.plugin-publish") version "0.17.0"
    application
}

group = "site.ftka"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Core Libraries
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Adventure API
    val adventureVersion = "4.14.0"
    implementation("net.kyori:adventure-api:$adventureVersion")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-legacy:$adventureVersion")

    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Redis & Connection Pooling
    compileOnly("io.lettuce:lettuce-core:6.5.0.RELEASE")
    implementation("org.apache.commons:commons-pool2:2.4.3")

    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "8.12.1"
}

tasks.withType<Jar> {
    enabled = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "site.ftka.proxycore.MClass"
    }

    val outputDir = findProperty("outputDir") as String? ?: "C:/Users/srleg/Desktop/server/plugins/"
    destinationDirectory = layout.buildDirectory.dir(outputDir)

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get().files.map { zipTree(it) }
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}

application {
    mainClass.set("site.ftka.proxycore.MClass")
}
