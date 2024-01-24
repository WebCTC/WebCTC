plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

val serializationVersion = extra["kotlinx.serialization.version"] as String
kotlin {
    jvm()
    js {
        binaries.executable()
        browser { }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

                implementation("app.softwork:kotlinx-uuid-core:0.0.22")
            }
        }
    }
}