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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect")) // Needed for proprietary events
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20-Beta1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Adventure dependencies
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.14.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.14.0")

    // paper
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("io.lettuce:lettuce-core:6.5.0.RELEASE") // redis
    implementation("org.apache.commons:commons-pool2:2.4.3") // connection pooling
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper<Wrapper> {
    gradleVersion = "8.5"
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
        configurations.compileClasspath.get().filter {
            it.name.endsWith("jar")
        }.map { zipTree(it) }
    }) {
        exclude("META-INF/*.RSA\", \"META-INF/*.SF\", \"META-INF/*.DSA")
    }
}

application {
    mainClass.set("MClass")
}