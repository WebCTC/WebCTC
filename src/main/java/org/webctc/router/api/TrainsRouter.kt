package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.parts.EntityFloor
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter

class TrainsRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.send(
                gson.toJson(
                    WebCTCCore.INSTANCE.server.entityWorld.loadedEntityList
                        .filterIsInstance<EntityTrainBase>().map(EntityTrainBase::toMutableMap)
                )
            )
        }

        get("/:entityId") { req, res ->
            val eId = req.getParam("entityId")
            val entity = WebCTCCore.INSTANCE.server.entityWorld.getEntityByID(eId.toInt())

            res.contentType = MediaType._json.mime
            res.send(gson.toJson(entity?.let { (it as? EntityTrainBase)?.toMutableMap() }))
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
    jsonMap["driver"] = this.riddenByEntity?.commandSenderName
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

    return jsonMap
}