package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.uuid.toUUID
import org.webctc.WebCTCCore
import org.webctc.cache.tecon.TeConData
import org.webctc.cache.tecon.delete
import org.webctc.common.types.tecon.TeCon
import org.webctc.router.WebCTCRouter

class TeConRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get {
            call.respond(TeConData.teConList.values.toList())
        }

        get("/{TeCon}") {
            val teCon = call.getTeCon() ?: return@get
            call.respond(teCon)
        }

        authenticate("auth-session") {
            post {
                val teCon = TeConData.create()
                call.respond(teCon)

                WebCTCCore.INSTANCE.teConData.markDirty()
            }

            route("/{TeCon}") {
                put {
                    val tecon = call.getTeCon() ?: return@put
                    val newTeCon = call.receive<TeCon>()
                    tecon.updateBy(newTeCon)
                    call.respond(HttpStatusCode.OK)

                    WebCTCCore.INSTANCE.teConData.markDirty()
                }

                delete {
                    val teCon = call.getTeCon() ?: return@delete
                    teCon.delete()
                    call.respond(HttpStatusCode.OK)

                    WebCTCCore.INSTANCE.teConData.markDirty()
                }
            }
        }
    }
}

private fun ApplicationCall.getTeCon() = parameters["TeCon"]?.toUUID()?.let { TeConData.teConList[it] }