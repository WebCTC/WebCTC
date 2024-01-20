package org.webctc.router

import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

class SpaRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        singlePageApplication {
            useResources = true
            filesPath = "assets/webctc/html"
            ignoreFiles { it.endsWith(".txt") }
        }
        authenticate("auth-session") {
            route("/p") {
                singlePageApplication {
                    useResources = true
                    filesPath = "assets/webctc/html"
                    ignoreFiles { it.endsWith(".txt") }
                }
            }
        }
    }
}