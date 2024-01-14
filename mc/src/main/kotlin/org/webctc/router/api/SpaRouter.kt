package org.webctc.router.api

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.webctc.router.WebCTCRouter

class SpaRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        singlePageApplication {
            useResources = true
            filesPath = "assets/webctc/html"
            ignoreFiles { it.endsWith(".txt") }
        }
    }
}