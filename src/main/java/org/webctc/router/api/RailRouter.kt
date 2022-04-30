package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore
import jp.ngt.rtm.rail.util.RailMap
import jp.ngt.rtm.rail.util.RailMapSwitch
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.util.MathHelper
import org.webctc.WebCTCCore
import org.webctc.cache.rail.RailCacheData
import org.webctc.router.WebCTCRouter

class RailRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            this.call.respondText(ContentType.Application.Json) {
                gson.toJson(RailCacheData.railMapCache.values.filter {
                    call.request.queryParameters["lite"] != "true" ||
                            it["isTrainOnRail"] == true ||
                            (it["railMaps"] as List<Map<String, Any>>).any { it["isNotActive"] == true }
                })
            }
        }
        get("/rail") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val x = call.request.queryParameters["x"]?.toIntOrNull()
            val y = call.request.queryParameters["y"]?.toIntOrNull()
            val z = call.request.queryParameters["z"]?.toIntOrNull()
            var railCore: TileEntityLargeRailCore? = null
            if (x != null && y != null && z != null) {
                railCore = WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z) as? TileEntityLargeRailCore
            }
            this.call.respondText(ContentType.Application.Json) { gson.toJson(railCore?.toMutableMap()) }
        }
    }
}

fun TileEntityLargeRailCore.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    try {
        jsonMap["pos"] = this.startPoint
        jsonMap["isTrainOnRail"] = this.isTrainOnRail
        jsonMap["railMaps"] = this.getNeighborRailMaps()
//    jsonMap["isCache"] = false
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return jsonMap
}

fun TileEntityLargeRailCore.getNeighborRailMaps(): List<Map<String, Any>> {
    if (this is TileEntityLargeRailSwitchCore) {
        this.switch.onBlockChanged(worldObj)
    }
    return this.allRailMaps.map { it.toMutableMap() }
}

private val isOpenField = RailMapSwitch::class.java.getDeclaredField("isOpen").apply {
    isAccessible = true
}

fun RailMap.toMutableMap(): Map<String, Any> {
    return if (this is RailMapSwitch)
        this.toMutableMap()
    else
        mapOf(
            "startRP" to this.startRP,
            "endRP" to this.endRP,
            "length" to this.length,
            "neighborPos" to mapOf(
                "startPR" to this.startRP.getNeighborPos(),
                "endRP" to this.endRP.getNeighborPos()
            ),
        )
}

fun RailMapSwitch.toMutableMap(): Map<String, Any> {
    return mapOf(
        "startRP" to this.startRP,
        "endRP" to this.endRP,
        "length" to this.length,
        "neighborPos" to mapOf(
            "startPR" to this.startRP.getNeighborPos(),
            "endRP" to this.endRP.getNeighborPos()
        ),
        "isNotActive" to !isOpenField[this].toString().toBoolean()
    )
}

fun RailPosition.getNeighborPos(): Map<String, Int> {
    return mapOf(
        "x" to MathHelper.floor_double(this.posX + RailPosition.REVISION[this.direction.toInt()][0]),
        "y" to this.blockY,
        "z" to MathHelper.floor_double(this.posZ + RailPosition.REVISION[this.direction.toInt()][1])
    )
}