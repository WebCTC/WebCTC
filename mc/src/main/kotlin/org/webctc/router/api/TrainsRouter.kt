package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.parts.EntityFloor
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter

class TrainsRouter : WebCTCRouter() {

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respond(
                WebCTCCore.INSTANCE.server.entityWorld.loadedEntityList
                    .filterIsInstance<EntityTrainBase>().map(EntityTrainBase::toMutableMap)
            )
        }
        get("/{EntityId}") {
            call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val eId = call.parameters["EntityId"]?.toInt()
            val entity = eId?.let { WebCTCCore.INSTANCE.server.entityWorld.getEntityByID(it) }

            if (entity == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(entity.let { (it as EntityTrainBase).toMutableMap() })
            }
        }
    }
}

fun EntityTrainBase.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["formation"] = this.formation?.id
    jsonMap["id"] = this.entityId
    jsonMap["speed"] = this.speed
    jsonMap["notch"] = this.notch
    jsonMap["modelName"] = this.modelName
    jsonMap["isControlCar"] = this.isControlCar
    jsonMap["signal"] = this.signal
    jsonMap["driver"] = this.riddenByEntity?.commandSenderName ?: ""
    jsonMap["passengers"] =
        EntityVehicleBase::class.java.getDeclaredField("vehicleFloors")
            .apply { isAccessible = true }.get(this)
            ?.let { vehicleFloors ->
                (vehicleFloors as List<*>)
                    .filterIsInstance(EntityFloor::class.java)
                    .map { entityFloor -> entityFloor.riddenByEntity?.commandSenderName }
            }
    jsonMap["pos"] = arrayOf(this.posX, this.posY, this.posZ)
    jsonMap["trainStateData"] =
        (EntityTrainBase::class.java.getDeclaredMethod("getByteArray")
            .apply { isAccessible = true }.invoke(this))
    jsonMap["name"] = this.resourceState.name
    jsonMap["customButton"] = this.modelSet.config.customButtons.mapIndexed { i, list ->
        val value = this.resourceState.dataMap.getInt("Button$i")
        val text = if (list.size > value) list[value] else null
        mapOf("value" to value, "text" to text)
    }.toList()
    jsonMap["dataMap"] = this.resourceState.dataMap.entries.map { it.key to it.value.get() }.toMap()
    return jsonMap
}