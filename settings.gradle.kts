pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
                includeGroup("com.gtnewhorizons")
            }
        }
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String

        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.js-plain-objects") version kotlinVersion

        id("com.gtnewhorizons.gtnhsettingsconvention") version ("2.0.2")
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