package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.parts.EntityFloor
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import org.webctc.WebCTCCore
import org.webctc.common.types.PosDouble
import org.webctc.common.types.trains.CustomButtonData
import org.webctc.common.types.trains.TrainData
import org.webctc.router.WebCTCRouter

class TrainsRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get {
            call.respond(
                WebCTCCore.INSTANCE.server.entityWorld.loadedEntityList
                    .filterIsInstance<EntityTrainBase>().map(EntityTrainBase::toData)
            )
        }
        get("/{EntityId}") {
            val eId = call.parameters["EntityId"]?.toInt()
            val entity = eId?.let { WebCTCCore.INSTANCE.server.entityWorld.getEntityByID(it) }

            if (entity == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(entity.let { (it as EntityTrainBase).toData() })
            }
        }
    }
}

fun EntityTrainBase.toData(): TrainData {
    return TrainData(
        this.formation?.id,
        this.entityId,
        this.speed,
        this.notch,
        this.modelName,
        this.isControlCar,
        this.signal,
        this.riddenByEntity?.commandSenderName ?: "",
        EntityVehicleBase::class.java.getDeclaredField("vehicleFloors")
            .apply { isAccessible = true }.get(this)
            ?.let { vehicleFloors ->
                (vehicleFloors as List<*>)
                    .filterIsInstance<EntityFloor>()
                    .map { entityFloor -> entityFloor.riddenByEntity?.commandSenderName }
            } ?: listOf(),
        PosDouble(this.posX, this.posY, this.posZ),
        ((EntityTrainBase::class.java.getDeclaredMethod("getByteArray")
            .apply { isAccessible = true }.invoke(this)) as ByteArray),
        this.resourceState.name,
        this.modelSet.config.customButtons.mapIndexed { i, list ->
            val value = this.resourceState.dataMap.getInt("Button$i")
            val text = if (list.size > value) list[value] else null
            CustomButtonData(value, text)
        },
//        this.resourceState.dataMap.entries.map { it.key to it.value.get() }.toMap()
    )
}