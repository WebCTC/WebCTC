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
        kotlin("plugin.serialization") version kotlinVersion
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "forge" -> useModule("com.anatawa12.forge:ForgeGradle:1.2-1.1.+")
            }
        }
    }
}

rootProject.name = "WebCTC"
include("front")