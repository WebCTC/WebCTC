package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore
import jp.ngt.rtm.rail.util.RailMap
import jp.ngt.rtm.rail.util.RailMapSwitch
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.server.MinecraftServer
import net.minecraft.util.MathHelper
import org.webctc.WebCTCCore
import org.webctc.cache.Pos
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.rail.data.*
import org.webctc.router.WebCTCRouter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class RailRouter : WebCTCRouter() {
    companion object {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    }

    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            this.call.respondText(ContentType.Application.Json) {
                gson.toJson(RailCacheData.railMapCache.values)
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
        webSocket("/railsocket") {
            val thisConnection = Connection(this)
            try {
                connections += thisConnection
                for (frame in incoming) {
                }
            } catch (e: Exception) {
                MinecraftServer.getServer().logWarning(e.message)
            }
            connections -= thisConnection
        }
    }
}

fun TileEntityLargeRailCore.toMutableMap(): LargeRailData {
    return LargeRailData(
        this.startPoint,
        this.isTrainOnRail,
        this.getNeighborRailMaps()
    )
}

fun TileEntityLargeRailCore.getNeighborRailMaps(): List<IRailMapData> {
    if (this is TileEntityLargeRailSwitchCore) {
        this.switch.onBlockChanged(worldObj)
    }
    return this.allRailMaps.map { it.toMutableMap() }
}

private val isOpenField = RailMapSwitch::class.java.getDeclaredField("isOpen").apply {
    isAccessible = true
}

fun RailMap.toMutableMap(): IRailMapData {
    return if (this is RailMapSwitch)
        this.toMutableMap()
    else
        RailMapData(
            this.startRP,
            this.endRP,
            this.length,
            NeighborPos(
                this.startRP.getNeighborPosData(),
                this.endRP.getNeighborPosData()
            ),
        )
}

fun RailMapSwitch.toMutableMap(): IRailMapData {
    return RailMapSwitchData(
        this.startRP,
        this.endRP,
        this.length,
        NeighborPos(
            this.startRP.getNeighborPosData(),
            this.endRP.getNeighborPosData()
        ),
        !isOpenField[this].toString().toBoolean()
    )
}

fun RailPosition.getNeighborPosData(): Pos {
    return Pos(
        MathHelper.floor_double(this.posX + RailPosition.REVISION[this.direction.toInt()][0]),
        this.blockY,
        MathHelper.floor_double(this.posZ + RailPosition.REVISION[this.direction.toInt()][1])
    )
}

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}