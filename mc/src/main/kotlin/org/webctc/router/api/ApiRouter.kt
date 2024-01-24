package org.webctc.router.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.router.AbstractRouter

class ApiRouter : AbstractRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            val req = this.context.request
            val scheme = req.local.scheme
            val removeHost = req.local.remoteHost
            val uri = req.local.uri
            this.call.respondText {
                """
                This is WebCTC API. 
                $scheme://$removeHost${uri}
                $scheme://$removeHost${uri}formations/
                $scheme://$removeHost${uri}formations/<formationId>
                $scheme://$removeHost${uri}formations/<formationId>/trains
                $scheme://$removeHost${uri}trains/
                $scheme://$removeHost${uri}trains/<entityId>
                $scheme://$removeHost${uri}rails/
                $scheme://$removeHost${uri}rails/rail?x=<x>&y=<y>&z=<z>
                $scheme://$removeHost${uri}signals/
                $scheme://$removeHost${uri}signals/signal?x=<x>&y=<y>&z=<z>
                """.trimIndent()
            }
        }
    }
}