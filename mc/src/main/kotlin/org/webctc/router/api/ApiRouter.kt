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
                http://$removeHost${uri}
                http://$removeHost${uri}formations/
                http://$removeHost${uri}formations/<formationId>
                http://$removeHost${uri}formations/<formationId>/trains
                http://$removeHost${uri}trains/
                http://$removeHost${uri}trains/<entityId>
                http://$removeHost${uri}rails/
                http://$removeHost${uri}rails/rail?x=<x>&y=<y>&z=<z>
                http://$removeHost${uri}signals/
                http://$removeHost${uri}signals/signal?x=<x>&y=<y>&z=<z>
                """.trimIndent()
            }
        }
    }
}