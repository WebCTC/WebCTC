plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    kotlin("plugin.js-plain-objects") apply false

    id("com.gtnewhorizons.gtnhsettingsconvention") apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}