package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.rtm.CommonProxy
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityBogie
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import jp.ngt.rtm.entity.train.util.FormationManager
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import net.minecraft.entity.player.EntityPlayer
import org.webctc.common.types.trains.FormationData
import org.webctc.common.types.trains.FormationEntityData
import org.webctc.common.types.trains.TrainData
import org.webctc.openapi.OpenApiRoute
import org.webctc.router.WebCTCRouter

class FormationsRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        @OpenApiRoute(summary = "List formations", response = FormationData::class, responseList = true)
        get {
            call.respond(getServerFormationManager().formations.values.mapNotNull { it.toData() })
        }
        @OpenApiRoute(summary = "Get a formation", response = FormationData::class)
        get("/{FormationID}") {
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = this@FormationsRouter.getFormation(formationId)

            if (formation == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(formation.toData())
            }
        }
        @OpenApiRoute(summary = "List trains in a formation", response = TrainData::class, responseList = true)
        get("/{FormationID}/trains") {
            val formationId = call.parameters["FormationID"]!!.toLong()
            val formation = this@FormationsRouter.getFormation(formationId)

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

    private fun getFormation(formationId: Long): Formation? {
        return getServerFormationManager().getFormation(formationId)
    }
}

fun Formation.toData(): FormationData {
    val controlCar = this.getControlCar()
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
        controlCar?.speed ?: 0f,
        this.getCurrentRailObj()?.toData()?.pos
    )
}

fun Formation.getControlCar(): EntityTrainBase? {
    return Formation::class.java.getDeclaredMethod("getControlCar")
        .apply { isAccessible = true }.invoke(this) as? EntityTrainBase
}

fun Formation.getCurrentRailObj(): TileEntityLargeRailCore? {
    val controlCar = this.getControlCar()
    val frontBogie =
        if (controlCar?.getBogie(0)?.isFront == true) controlCar.getBogie(0)
        else controlCar?.getBogie(1)

    return EntityBogie::class.java.getDeclaredField("currentRailObj")
        .apply { isAccessible = true }.get(frontBogie) as? TileEntityLargeRailCore
}
