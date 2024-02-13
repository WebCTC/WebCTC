package org.webctc

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
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
import kotlinx.uuid.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.util.EnumChatFormatting.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.webctc.cache.auth.CredentialData
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.signal.SignalCacheData
import org.webctc.cache.tecon.TeConData
import org.webctc.cache.waypoint.WayPointCacheData
import org.webctc.command.CommandWebCTC
import org.webctc.common.types.kotlinxJson
import org.webctc.plugin.PluginManager
import org.webctc.plugin.webauthn.WebAuthnChallenge
import org.webctc.railgroup.RailGroupData
import org.webctc.router.AuthRouter
import org.webctc.router.RouterManager
import org.webctc.router.SpaRouter
import org.webctc.router.api.*
import java.security.SecureRandom
import java.time.Duration

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var applicationEngine: ApplicationEngine
    lateinit var railData: RailCacheData
    lateinit var signalData: SignalCacheData
    lateinit var wayPointData: WayPointCacheData
    lateinit var credentialData: CredentialData
    lateinit var railGroupData: RailGroupData
    lateinit var teConData: TeConData

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        WebCTCConfig.preInit(Configuration(event.suggestedConfigurationFile))

        val eventHandler = WebCTCEventHandler()
        FMLCommonHandler.instance().bus().register(eventHandler)
        MinecraftForge.EVENT_BUS.register(eventHandler)
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
        RouterManager.registerRouter("/api/railgroups", RailGroupRouter())
        RouterManager.registerRouter("/api/tecons", TeConRouter())
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

        var railData = world.mapStorage.loadData(RailCacheData::class.java, "webctc_railcache_v2")
        if (railData == null) {
            railData = RailCacheData("webctc_railcache_v2")
            world.mapStorage.setData("webctc_railcache_v2", railData)
        }
        this.railData = railData as RailCacheData

        var signalData = world.mapStorage.loadData(SignalCacheData::class.java, "webctc_signalcache_v2")
        if (signalData == null) {
            signalData = SignalCacheData("webctc_signalcache_v2")
            world.mapStorage.setData("webctc_signalcache_v2", signalData)
        }
        this.signalData = signalData as SignalCacheData

        var wayPointData = world.mapStorage.loadData(WayPointCacheData::class.java, "webctc_waypointcache")
        if (wayPointData == null) {
            wayPointData = WayPointCacheData("webctc_waypointcache")
            world.mapStorage.setData("webctc_waypointcache", wayPointData)
        }
        this.wayPointData = wayPointData as WayPointCacheData

        val credentialData = world.mapStorage.loadData(CredentialData::class.java, "webctc_webauthn_credential")
        if (credentialData == null) {
            this.credentialData = CredentialData("webctc_webauthn_credential")
            world.mapStorage.setData("webctc_webauthn_credential", this.credentialData)
        } else {
            this.credentialData = credentialData as CredentialData
        }

        val railGroupData = world.mapStorage.loadData(RailGroupData::class.java, "webctcex_railgroup")
        if (railGroupData == null) {
            this.railGroupData = RailGroupData("webctcex_railgroup")
            world.mapStorage.setData("webctcex_railgroup", this.railGroupData)
        } else {
            this.railGroupData = railGroupData as RailGroupData
        }

        val teConData = world.mapStorage.loadData(TeConData::class.java, "webctc_tecon")
        if (teConData == null) {
            this.teConData = TeConData("webctc_tecon")
            world.mapStorage.setData("webctc_tecon", this.teConData)
        } else {
            this.teConData = teConData as TeConData
        }

        this.applicationEngine = embeddedServer(Netty, port = WebCTCConfig.portNumber) {
            PluginManager.pluginList.forEach { it(this) }
            routing {
                RouterManager.routerMap.forEach { (path, router) -> route(path, router.install(this)) }
            }
        }.start()

    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        applicationEngine.stop()
        railData.markDirty()
        signalData.markDirty()
    }

    companion object {
        const val MODID = "webctc"
        const val VERSION = "0.5.0"

        val IN_CHAT_LOGO = "${GRAY}[${GREEN}Web${WHITE}CTC${GRAY}]"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}