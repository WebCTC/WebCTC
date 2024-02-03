package org.webctc.router.api

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.router.AbstractRouter

class ApiRouter : AbstractRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get {
            val req = context.request
            val scheme = req.origin.scheme
            val remoteHost = req.origin.serverHost
            val remotePort = req.origin.serverPort.let {
                if (it == 80 && scheme == "http" || it == 443 && scheme == "https") "" else ":$it"
            }
            val origin = "$scheme://$remoteHost$remotePort"
            val uri = req.origin.uri
            call.respondText {
                """
                This is WebCTC API. 
                $origin$uri
                $origin$uri/formations
                $origin$uri/formations/<formationId>
                $origin$uri/formations/<formationId>/trains
                $origin$uri/trains
                $origin$uri/trains/<entityId>
                $origin$uri/rails
                $origin$uri/rails/rail?x=<x>&y=<y>&z=<z>
                $origin$uri/signals
                $origin$uri/signals/signal?x=<x>&y=<y>&z=<z>
                """.trimIndent()
            }
        }
    }
}