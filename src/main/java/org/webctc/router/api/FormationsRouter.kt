package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.rtm.CommonProxy
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import jp.ngt.rtm.entity.train.util.FormationManager
import net.minecraft.entity.player.EntityPlayer
import org.webctc.router.WebCTCRouter

class FormationsRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            res.send(gson.toJson(this.getServerFormationManager().formations.values.mapNotNull { Formation::toMutableMap }))
        }

        get("/:formationId") { req, res ->
            val formationId = req.getParam("formationId")
            val formation = this.getServerFormationManager().getFormation(formationId.toLong())

            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            res.send(gson.toJson(formation?.toMutableMap()))
        }
        get("/:formationId/trains") { req, res ->
            val formationId = req.getParam("formationId")
            val formation = this.getServerFormationManager().getFormation(formationId.toLong())

            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            res.send(gson.toJson(formation?.let { it.entries.mapNotNull { entry -> entry.train.toMutableMap() } }))
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
    jsonMap["entries"] = this.entries
        .mapNotNull {
            mutableMapOf<String, Any?>(
                "train" to (it.train?.entityId ?: 0),
                "entryId" to it.entryId,
                "dir" to it.dir
            )
        }
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