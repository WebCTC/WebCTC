package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.rtm.CommonProxy
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import jp.ngt.rtm.entity.train.util.FormationManager
import net.minecraft.entity.player.EntityPlayer
import org.webctc.router.WebCTCRouter

class FormationsRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respond(getServerFormationManager().formations.values.mapNotNull { it.toMutableMap() })
        }
        get("/{FormationID}") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = this@FormationsRouter.getServerFormationManager().getFormation(formationId)

            if (formation == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(formation.toMutableMap())
            }
        }
        get("/{FormationID}/trains") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = getServerFormationManager().getFormation(formationId)

            val trains = formation?.entries?.mapNotNull { it.train.toMutableMap() }

            if (trains == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(trains)
            }
        }
    }

    private fun getServerFormationManager(): FormationManager {
        return CommonProxy::class.java.getDeclaredField("fm")
            .apply { isAccessible = true }
            .get(RTMCore.proxy) as FormationManager
    }
}

fun Formation.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["id"] = this.id
    jsonMap["entries"] = this.entries?.mapNotNull {
        mapOf<String, Any>(
            "train" to (it.train?.entityId ?: 0),
            "entryId" to it.entryId,
            "dir" to it.dir
        )
    } ?: emptyList<Map<String, Any>>()
    val controlCar = Formation::class.java.getDeclaredMethod("getControlCar")
        .apply { isAccessible = true }.invoke(this) as? EntityTrainBase
    jsonMap["controlCar"] = controlCar?.toMutableMap()

    val driver = controlCar?.riddenByEntity as? EntityPlayer
    jsonMap["driver"] = driver?.commandSenderName
    jsonMap["direction"] = Formation::class.java.getDeclaredField("direction")
        .apply { isAccessible = true }.getByte(this)
    jsonMap["speed"] = controlCar?.speed ?: 0f

    return jsonMap
}