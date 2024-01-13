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
import org.webctc.common.types.trains.FormationData
import org.webctc.common.types.trains.FormationEntityData
import org.webctc.router.WebCTCRouter

class FormationsRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            try {
                println(getServerFormationManager().formations.values.mapNotNull { it.toData() })
                call.respond(getServerFormationManager().formations.values.mapNotNull { it.toData() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        get("/{FormationID}") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = this@FormationsRouter.getServerFormationManager().getFormation(formationId)

            if (formation == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(formation.toData())
            }
        }
        get("/{FormationID}/trains") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = getServerFormationManager().getFormation(formationId)

            val trains = formation?.entries?.mapNotNull { it.train.toData() }

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

fun Formation.toData(): FormationData {

    val controlCar = Formation::class.java.getDeclaredMethod("getControlCar")
        .apply { isAccessible = true }.invoke(this) as? EntityTrainBase

    val driver = controlCar?.riddenByEntity as? EntityPlayer
    return FormationData(

        this.id,
        this.entries?.mapNotNull {
            FormationEntityData(
                it.train?.entityId ?: 0,
                it.entryId,
                it.dir
            )
        } ?: listOf(),
        controlCar?.toData(),
        driver?.commandSenderName ?: "",
        Formation::class.java.getDeclaredField("direction")
            .apply { isAccessible = true }.getByte(this),
        controlCar?.speed ?: 0f
    )

}