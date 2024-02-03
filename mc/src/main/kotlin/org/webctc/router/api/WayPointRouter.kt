package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.webctc.WebCTCCore
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.common.types.waypoint.WayPoint
import org.webctc.router.WebCTCRouter

class WayPointRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get {
            try {
                call.respond(WayPointCacheData.wayPointCache.values)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        authenticate("auth-session") {
            route("{id}") {
                patch {
                    val id = call.parameters["id"] ?: return@patch

                    val wayPoint: WayPoint = call.receive()

                    WayPointCacheData.wayPointCache[id] = wayPoint
                    WebCTCCore.INSTANCE.wayPointData.markDirty()

                    call.respond(wayPoint)
                }

                delete("/delete") {
                    val id = call.parameters["id"] ?: return@delete

                    WayPointCacheData.wayPointCache.remove(id)
                    WebCTCCore.INSTANCE.wayPointData.markDirty()

                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}