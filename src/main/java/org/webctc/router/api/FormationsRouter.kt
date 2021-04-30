package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import jp.ngt.rtm.entity.train.util.FormationManager
import org.webctc.router.WebCTCRouter

class ApiFormationsRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.send(gson.toJson(FormationManager.getInstance().formations.values.map(Formation::toMutableMap)))
        }

        get("/:formationId") { req, res ->
            val formationId = req.getParam("formationId")
            val formation = FormationManager.getInstance().getFormation(formationId.toLong())

            res.contentType = MediaType._json.mime
            res.send(gson.toJson(formation?.toMutableMap()))
        }
        get("/:formationId/trains") { req, res ->
            val formationId = req.getParam("formationId")
            val formation = FormationManager.getInstance().getFormation(formationId.toLong())

            res.contentType = MediaType._json.mime
            res.send(gson.toJson(formation?.let { it.entries.map { entry -> entry.train.toMutableMap() } }))
        }
    }
}

fun Formation.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["id"] = this.id
    jsonMap["entries"] = this.entries
        .filterNotNull()
        .map {
            mutableMapOf<String, Any?>(
                "train" to (it.train?.entityId ?: 0),
                "entryId" to it.entryId,
                "dir" to it.dir
            )
        }
    val controlCar = (Formation::class.java.getDeclaredField("controlCar")
        .apply { isAccessible = true }.get(this))
    jsonMap["controlCar"] = controlCar?.let { (it as EntityTrainBase).entityId }
    jsonMap["driver"] = controlCar?.let { (it as EntityTrainBase).riddenByEntity?.commandSenderName }
    jsonMap["direction"] = Formation::class.java.getDeclaredField("direction")
        .apply { isAccessible = true }.getByte(this)
    jsonMap["speed"] = controlCar?.let { (it as EntityTrainBase).speed } ?: 0f

    return jsonMap
}