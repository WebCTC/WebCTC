package org.webctc

import com.google.gson.GsonBuilder
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import express.Express
import express.utils.MediaType
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.parts.EntityFloor
import jp.ngt.rtm.entity.train.util.Formation
import jp.ngt.rtm.entity.train.util.FormationEntry
import jp.ngt.rtm.entity.train.util.FormationManager
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import net.minecraft.server.MinecraftServer

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    var server: MinecraftServer? = null

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
    }

    @Mod.EventHandler
    fun onServerStart(event: FMLServerStartingEvent) {
        server = event.server
        object : Express() {
            init {
                val gson = GsonBuilder()
                    .serializeNulls()
                    .disableHtmlEscaping()
                    .create()

                get("/") { req, res ->
                    res.send("Hello World!")
                }

                get("/formations") { req, res ->
                    res.contentType = MediaType._json.mime
                    res.send(gson.toJson(FormationManager.getInstance().formations.values.map(Formation::toMutableMap)))
                }

                get("/formations/:formationId") { req, res ->
                    val formationId = req.getParam("formationId")
                    val formation = FormationManager.getInstance().getFormation(formationId.toLong())

                    res.contentType = MediaType._json.mime
                    res.send(gson.toJson(formation?.toMutableMap()))
                }

                get("/formations/:fid/trains") { req, res ->
                    val fId = req.getParam("fid")
                    val formation = FormationManager.getInstance().getFormation(fId.toLong())

                    res.contentType = MediaType._json.mime
                    res.send(
                        gson.toJson(
                            formation?.let {
                                it.entries
                                    .map { FormationEntry::train }
                                    .map { EntityTrainBase::toMutableMap }
                            }
                        )
                    )
                }

                get("/trains") { req, res ->
                    res.contentType = MediaType._json.mime
                    res.send(
                        gson.toJson(
                            event.server.entityWorld.loadedEntityList
                                .filterIsInstance<EntityTrainBase>()
                                .map { EntityTrainBase::toMutableMap }
                        )
                    )
                }

                get("/trains/:eid") { req, res ->
                    val eId = req.getParam("eid")
                    val entity = event.server.entityWorld.getEntityByID(eId.toInt())

                    res.contentType = MediaType._json.mime
                    res.send(
                        gson.toJson(
                            entity?.let { (it as? EntityTrainBase)?.toMutableMap() }
                        )
                    )
                }

                listen(8080)
            }
        }
    }

    companion object {
        const val MODID = "WebCTC"
        const val VERSION = "1.0-SNAPSHOT"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}

fun Formation.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["id"] = this.id
    jsonMap["entries"] = this.entries
        .map {
            mutableMapOf<String, Any>(
                "train" to it.train.entityId,
                "entryId" to it.entryId,
                "dir" to it.dir,
            )
        }
    val controlCar = (Formation::class.java.getDeclaredField("controlCar")
        .apply { isAccessible = true }.get(this))
    jsonMap["controlCar"] = controlCar?.let { (it as EntityTrainBase).entityId }
    jsonMap["driver"] = controlCar?.let { (it as EntityTrainBase).riddenByEntity?.commandSenderName }
    jsonMap["direction"] = Formation::class.java.getDeclaredField("direction")
        .apply { isAccessible = true }.getByte(this)
    jsonMap["speed"] = controlCar?.let { (it as EntityTrainBase).speed } ?: 0

    return jsonMap
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