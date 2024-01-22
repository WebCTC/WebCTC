import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize

plugins {
    kotlin("multiplatform")
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

val wrappersVersion = extra["kotlin.wrappers.version"] as String
val ktorVersion = extra["ktor.version"] as String
fun ktor(target: String) = "io.ktor:ktor-$target:$ktorVersion"
fun ktorCl(target: String) = ktor("client-$target")
fun kotlinWrp(target: String) = "org.jetbrains.kotlin-wrappers:kotlin-$target"

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(ktorCl("js"))
                implementation(ktorCl("websockets"))
                implementation(ktorCl("content-negotiation"))
                implementation(ktor("serialization-kotlinx-json"))

                implementation(project.dependencies.enforcedPlatform(kotlinWrp("wrappers-bom:$wrappersVersion")))
                implementation(kotlinWrp("react"))
                implementation(kotlinWrp("react-dom"))
                implementation(kotlinWrp("react-router-dom"))

                implementation(kotlinWrp("emotion"))
                implementation(kotlinWrp("mui-material"))
                implementation(kotlinWrp("mui-icons-material"))

                implementation(npm("panzoom", "9.4.0"))

                implementation(project(":common"))
            }
        }
    }
}

tasks.named("jsProcessResources") {
    dependsOn("createSPAHtml")
}

task("createSPAHtml") {
    File(project.projectDir, "src/jsMain/resources/index.html").writeText(
        createHTMLDocument().html {
            head {
                meta(charset = "utf-8")
                title("Viewer | WebCTC")
                meta(
                    name = "viewport",
                    content = "width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"
                )
                link(
                    rel = "stylesheet",
                    href = "https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@300;400;500;600;700&display=swap"
                )
            }
            body {
                script(src = "/${project.name}.js") { }
            }
        }.serialize(true)
    )
}