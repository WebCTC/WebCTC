package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.router.WebCTCRouter

class WayPointRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            this.call.respondText(ContentType.Application.Json) { gson.toJson(WayPointCacheData.wayPointCache.values) }
        }
    }
}