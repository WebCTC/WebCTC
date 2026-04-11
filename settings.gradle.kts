pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String

        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.js-plain-objects") version kotlinVersion
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "forge" -> useModule("com.anatawa12.forge:ForgeGradle:1.2-1.1.+")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2026.4.4"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}

rootProject.name = "WebCTC"
include("front")
include("mc")
include("common")