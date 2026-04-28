import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    java
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gtnewhorizons.gtnhconvention")
}

val minecraftVersion = "1.7.10-10.13.4.1614-1.7.10"

group = "org.webctc"
version = "1.7.10-SNAPSHOT"

val sourcesJar by tasks.named<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val commonKotlin = project(":common").extensions.getByType<KotlinMultiplatformExtension>()
val commonJvmMain = commonKotlin.targets.getByName("jvm").compilations.getByName("main")
val frontProductionExecutable = project(":front").layout.buildDirectory.dir("dist/js/productionExecutable")

fun CopySpec.fromFrontendAssets() {
    from(frontProductionExecutable) {
        include("front.js", "index.html")
        into("assets/webctc/html")
    }
}

fun CopySpec.fromRootDocs() {
    from(rootDir) {
        include("README.md", "LICENSE")
    }
}

tasks.withType<Jar>().configureEach {
    exclude("module-info.class")
    exclude("META-INF/versions/**")
}

tasks.jar {
    dependsOn(":front:build")

    destinationDirectory.set(File(parent!!.buildDir, "libs"))

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    fromRootDocs()
    fromFrontendAssets()
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(":front:build")
    dependsOn(project(":common").tasks.named("jvmMainClasses"))
    from(commonJvmMain.output.allOutputs)
    fromRootDocs()
    fromFrontendAssets()

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
    }
}

tasks.register<Jar>("slimJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("slim")
    from(sourceSets.main.get().output)

    from(rootDir) {
        include("README.md")
        include("LICENCE")
    }
}

tasks.assemble {
    dependsOn("slimJar")
}

artifacts {
    add("archives", tasks.named("slimJar"))
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}
