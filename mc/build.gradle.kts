import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    java
    `java-gradle-plugin`
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("forge")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val minecraftVersion = "1.7.10-10.13.4.1614-1.7.10"

group = "org.webctc"
version = "1.7.10-SNAPSHOT"
base {
    archivesName.set("WebCTC")
}

minecraft {
    version = minecraftVersion
    runDir = "eclipse"
    srgExtra("PK: io/netty org/webctc/lib/io/netty")
    srgExtra("PK: org/slf4j org/webctc/lib/org/slf4j")
}

repositories {
    mavenCentral()
    maven(url = "https://www.cursemaven.com")
    maven(url = "https://jitpack.io")
}

val embed = configurations.create("embed") {
    configurations.getByName("api").extendsFrom(this)
}

val ktorVersion = extra["ktor.version"] as String
fun ktor(target: String) = "io.ktor:ktor-$target:$ktorVersion"
fun ktorSv(name: String) = ktor("server-$name")

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")

    embed(ktorSv("core"))
    embed(ktorSv("netty"))
    embed(ktorSv("compression"))
    embed(ktorSv("cors"))
    embed(ktorSv("websockets"))
    embed(ktorSv("content-negotiation"))
    embed(ktorSv("auth"))
    embed(ktorSv("sessions"))

    embed(ktor("serialization-kotlinx-json"))

    embed("com.webauthn4j:webauthn4j-core:0.21.9.RELEASE")

    embed("com.fasterxml.jackson.core:jackson-annotations:2.16.1")

    embed(project(":common"))

    api("com.github.Kai-Z-JP:KaizPatchX:-SNAPSHOT:dev")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    inputs.property("version", project.version)
    inputs.property("mcversion", project.minecraft.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand("version" to project.version, "mcversion" to project.minecraft.version)
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks.jar {
    dependsOn(":front:build")

    destinationDirectory.set(File(parent!!.buildDir, "libs"))

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    embed.forEach { dep ->
        from(project.zipTree(dep)) {
            exclude("kotlin/", "module-info.class", "META-INF/")
        }
    }

    from(parent!!.rootDir) {
        include("README.md", "LICENSE")
    }

    from(File(parent!!.subprojects.first { it.name == "front" }.buildDir, "dist/js/productionExecutable")) {
        include("front.js", "index.html")
        into("assets/webctc/html")
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
    jvmToolchain(8)
}