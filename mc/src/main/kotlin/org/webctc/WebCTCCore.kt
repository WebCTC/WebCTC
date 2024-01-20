package org.webctc

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.config.Configuration
import org.webctc.cache.auth.CredentialData
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
import org.webctc.plugin.webauthn.WebAuthnChallenge
import org.webctc.router.AuthRouter
import org.webctc.router.RouterManager
import org.webctc.router.SpaRouter
import org.webctc.router.api.*
import java.security.SecureRandom
import java.time.Duration
import java.util.*

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var applicationEngine: ApplicationEngine
    lateinit var railData: WorldSavedData
    lateinit var signalData: WorldSavedData
    lateinit var wayPointData: WorldSavedData
    lateinit var railCacheUpdate: RailCacheUpdate
    lateinit var signalCacheUpdate: SignalCacheUpdate
    lateinit var credentialData: WorldSavedData

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        WebCTCConfig.preInit(Configuration(event.suggestedConfigurationFile))
        FMLCommonHandler.instance().bus().register(this)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {

        PluginManager.registerPlugin {
            install(Compression)
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(5)
                contentConverter = KotlinxWebsocketSerializationConverter(kotlinxJson)
            }
            install(ContentNegotiation) {
                json(kotlinxJson)
            }
            val secretEncryptKey = SecureRandom.getInstanceStrong().generateSeed(16)
            val secretSignKey = SecureRandom.getInstanceStrong().generateSeed(16)
            install(Sessions) {
                cookie<UserSession>("user-session", SessionStorageMemory()) {
                    cookie.path = "/"
                    cookie.maxAgeInSeconds = 60 * 60 * 6
                    transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
                }
                cookie<WebAuthnChallenge>("webauthn-challenge", SessionStorageMemory()) {
                    transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
                }
            }
            install(Authentication) {
                session<UserSession>("auth-session") {
                    validate { session ->
                        if (session.uuid.toString().isNotEmpty()) session else null
                    }
                    challenge {
                        call.respondRedirect("/login")
                    }
                }
            }
        }

        RouterManager.registerRouter("/", SpaRouter())
        RouterManager.registerRouter("/api", ApiRouter())
        RouterManager.registerRouter("/api/formations", FormationsRouter())
        RouterManager.registerRouter("/api/trains", TrainsRouter())
        RouterManager.registerRouter("/api/rails", RailRouter())
        RouterManager.registerRouter("/api/signals", SignalRouter())
        RouterManager.registerRouter("/api/waypoints", WayPointRouter())
        RouterManager.registerRouter("/auth", AuthRouter())
    }

    data class UserSession(val id: String, val uuid: UUID) : Principal

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

        val credentialData = world.mapStorage.loadData(CredentialData::class.java, "webctc_webauthn_credential")
        if (credentialData == null) {
            this.credentialData = CredentialData("webctc_webauthn_credential")
            world.mapStorage.setData("webctc_webauthn_credential", this.credentialData)
        } else {
            this.credentialData = credentialData
        }

        this.applicationEngine = embeddedServer(Netty, port = WebCTCConfig.portNumber) {
            PluginManager.pluginList.forEach { it(this) }
            routing {
                RouterManager.routerMap.forEach { (path, router) -> route(path, router.install(this)) }
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


val kotlinxJson = Json {
    serializersModule = SerializersModule {
        polymorphic(IRailMapData::class) {
            subclass(RailMapData::class)
            subclass(RailMapSwitchData::class)
        }
    }
    ignoreUnknownKeys = true
}