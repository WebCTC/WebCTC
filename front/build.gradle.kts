import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize


plugins {
    kotlin("js")
}

buildscript {
    val kotlinxHtmlVersion = extra["kotlinx.html.version"] as String

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
    }

}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

}

val wrappersVersion = extra["kotlin.wrappers.version"] as String
val ktorVersion = extra["ktor.version"] as String

fun ktorCl(target: String) = "io.ktor:ktor-client-$target:$ktorVersion"
fun kotlinWrp(target: String) = "org.jetbrains.kotlin-wrappers:kotlin-$target"

dependencies {
    implementation(ktorCl("js"))

    implementation(enforcedPlatform(kotlinWrp("wrappers-bom:$wrappersVersion")))
    implementation(kotlinWrp("react"))
    implementation(kotlinWrp("react-dom"))
    implementation(kotlinWrp("react-router-dom"))

    implementation(kotlinWrp("emotion"))
    implementation(kotlinWrp("mui-material"))
    implementation(kotlinWrp("mui-icons-material"))
}

tasks.named("processResources") {
    dependsOn("createSPAHtml")
}

task("createSPAHtml") {
    File(project.projectDir, "src/main/resources/index.html").writeText(
        createHTMLDocument().html {
            head {
                meta(charset = "utf-8")
                title("Viewer | WebCTC")
            }
            body {
                script(src = "front.js") { }
            }
        }.serialize(true)
    )
}