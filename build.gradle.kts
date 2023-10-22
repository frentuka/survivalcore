plugins {
    kotlin("jvm") version "1.9.0"
    id("com.gradle.plugin-publish") version "0.17.0"
    application
}

group = "site.ftka"
version = "1.0-SNAPSHOT"

java {
    setTargetCompatibility(JavaVersion.VERSION_17)
    setSourceCompatibility(JavaVersion.VERSION_17)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT") // paper

    compileOnly("io.lettuce:lettuce-core:6.2.6.RELEASE") // redis
    implementation("org.apache.commons:commons-pool2:2.4.3") // connection pooling
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    enabled = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "site.ftka.proxycore.MClass"
    }

    destinationDirectory = layout.buildDirectory.dir("C:/Users/srleg/Desktop/testing/survival/plugins")

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