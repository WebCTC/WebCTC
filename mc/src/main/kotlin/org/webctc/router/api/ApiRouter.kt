package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.openapi.OpenApiRoute
import org.webctc.router.AbstractRouter

class ApiRouter : AbstractRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        @OpenApiRoute(summary = "List API entry points", response = String::class)
        get {
            val req = call.request
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
        @OpenApiRoute(summary = "Get the generated OpenAPI document", response = Map::class)
        get("/openapi.json") {
            val resource = Thread.currentThread().contextClassLoader
                .getResourceAsStream("assets/webctc/html/openapi.json")
            if (resource == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondText(
                    resource.bufferedReader().use { it.readText() },
                    ContentType.Application.Json
                )
            }
        }
        @OpenApiRoute(summary = "Open Swagger UI")
        get("/swagger") {
            call.respondRedirect("/swagger/index.html")
        }
    }
}
