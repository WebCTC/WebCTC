plugins {
    java
    `java-gradle-plugin`
    `java-library`
    kotlin("jvm")
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
    archivesBaseName = "WebCTC"
}

minecraft {
    version = minecraftVersion
    runDir = "eclipse"
    srgExtra("PK: io/netty org/webctc/lib/io/netty")
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
fun ktorSev(name: String) = "io.ktor:ktor-server-$name:$ktorVersion"

dependencies {
    embed("org.danilopianini:gson-extras:0.2.4")

    embed(ktorSev("core"))
    embed(ktorSev("netty"))
    embed(ktorSev("compression"))
    embed(ktorSev("cors"))
    embed(ktorSev("host-common"))
    embed(ktorSev("status-pages"))
    embed(ktorSev("websockets"))

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
    classifier = "sources"
}

tasks.jar {
    dependsOn("front:build")

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    embed.forEach { dep ->
        from(project.zipTree(dep)) {
            exclude("META-INF/", "kotlin/", "org/slf4j/")
        }
    }

    from(rootDir) {
        include("README.md", "LICENSE")
    }

    from(File(subprojects.first { it.name == "front" }.buildDir, "dist/js/productionExecutable")) {
        include("front.js", "index.html")
        into("assets/webctc/html")
    }
}