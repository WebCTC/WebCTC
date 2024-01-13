package org.webctc

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.config.Configuration
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.rail.RailCacheUpdate
import org.webctc.cache.signal.SignalCacheData
import org.webctc.cache.signal.SignalCacheUpdate
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.command.CommandWebCTC
import org.webctc.common.types.rail.IRailMapData
import org.webctc.common.types.rail.RailMapData
import org.webctc.common.types.rail.RailMapSwitchData
import org.webctc.plugin.PluginManager
import org.webctc.router.DefaultRouter
import org.webctc.router.RouterManager
import org.webctc.router.api.*
import java.time.Duration

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var applicationEngine: ApplicationEngine
    lateinit var railData: WorldSavedData
    lateinit var signalData: WorldSavedData
    lateinit var wayPointData: WorldSavedData
    lateinit var railCacheUpdate: RailCacheUpdate
    lateinit var signalCacheUpdate: SignalCacheUpdate

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        WebCTCConfig.preInit(Configuration(event.suggestedConfigurationFile))
        FMLCommonHandler.instance().bus().register(this)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        val jsonPreset = Json {
            serializersModule = SerializersModule {
                polymorphic(IRailMapData::class) {
                    subclass(RailMapData::class)
                    subclass(RailMapSwitchData::class)
                }
            }
            ignoreUnknownKeys = true
        }

        PluginManager.registerPlugin {
            install(Compression)
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(5)
                contentConverter = KotlinxWebsocketSerializationConverter(jsonPreset)
            }
            install(ContentNegotiation) {
                json(jsonPreset)
            }
        }

        RouterManager.registerRouter("/api", ApiRouter())
        RouterManager.registerRouter("/api/formations", FormationsRouter())
        RouterManager.registerRouter("/api/trains", TrainsRouter())
        RouterManager.registerRouter("/api/rails", RailRouter())
        RouterManager.registerRouter("/api/signals", SignalRouter())
        RouterManager.registerRouter("/api/waypoints", WayPointRouter())
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
    }

    @Mod.EventHandler
    fun handleServerStaring(event: FMLServerStartingEvent) {
        event.registerServerCommand(CommandWebCTC())
    }

    @Mod.EventHandler
    fun onServerStart(event: FMLServerStartedEvent) {
        server = MinecraftServer.getServer()
        val world = server.entityWorld

        var railData = world.mapStorage.loadData(RailCacheData::class.java, "webctc_railcache")
        if (railData == null) {
            railData = RailCacheData("webctc_railcache")
            world.mapStorage.setData("webctc_railcache", railData)
        }
        this.railData = railData

        var signalData = world.mapStorage.loadData(SignalCacheData::class.java, "webctc_signalcache")
        if (signalData == null) {
            signalData = SignalCacheData("webctc_signalcache")
            world.mapStorage.setData("webctc_signalcache", signalData)
        }
        this.signalData = signalData

        var wayPointData = world.mapStorage.loadData(WayPointCacheData::class.java, "webctc_waypointcache")
        if (wayPointData == null) {
            wayPointData = WayPointCacheData("webctc_waypointcache")
            world.mapStorage.setData("webctc_waypointcache", wayPointData)
        }
        this.wayPointData = wayPointData
        this.applicationEngine = embeddedServer(Netty, port = WebCTCConfig.portNumber) {
            PluginManager.pluginList.forEach { it(this) }
            routing {
                this.route("/", DefaultRouter().install(this))
                RouterManager.routerMap.forEach { (path, router) -> this.route(path, router.install(this)) }
            }
        }.start()

        railCacheUpdate = RailCacheUpdate()
        signalCacheUpdate = SignalCacheUpdate()
    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        applicationEngine.stop()
        railData.markDirty()
        signalData.markDirty()
    }

    private var tickCount = 0

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        tickCount++
        if (tickCount == 20 && event.phase.equals(TickEvent.Phase.END)) {
            railCacheUpdate.execute()
            signalCacheUpdate.execute()
            tickCount = 0
        }
    }

    companion object {
        const val MODID = "webctc"
        const val VERSION = "0.5.0"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}