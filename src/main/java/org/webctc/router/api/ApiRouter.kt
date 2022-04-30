package org.webctc.router.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.router.AbstractRouter

class ApiRouter : AbstractRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.respondText {
                "This is WebCTC API. \n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}formations/\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}formations/<formationId>\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}formations/<formationId>/trains\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}trains/\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}trains/<entityId>\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}rails/\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}rails/rail?x=<x>&y=<y>&z=<z>\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}signals/\n" +
                        "http://${this.context.request.local.remoteHost}${this.context.request.local.uri}signals/signal?x=<x>&y=<y>&z=<z>\n".trimIndent()
            }
        }
    }
}