package org.webctc

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import express.Express
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.config.Configuration
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.rail.RailCacheUpdate
import org.webctc.cache.signal.SignalCacheData
import org.webctc.cache.signal.SignalCacheUpdate
import org.webctc.router.DefaultRouter
import org.webctc.router.RouterManager
import org.webctc.router.api.*

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var express: Express
    lateinit var railData: WorldSavedData
    lateinit var signalData: WorldSavedData
    lateinit var railCacheUpdate: RailCacheUpdate
    lateinit var signalCacheUpdate: SignalCacheUpdate

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        WebCTCConfig.preInit(Configuration(event.suggestedConfigurationFile))
        FMLCommonHandler.instance().bus().register(this)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        RouterManager.registerRouter("/api", ApiRouter())
        RouterManager.registerRouter("/api/formations", FormationsRouter())
        RouterManager.registerRouter("/api/trains", TrainsRouter())
        RouterManager.registerRouter("/api/rails", RailRouter())
        RouterManager.registerRouter("/api/signals", SignalRouter())
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
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

        express = object : Express() {
            init {
                RouterManager.routerMap.forEach { (path, router) -> use(path, router::class.java.newInstance()) }
                use("/", DefaultRouter())
                all() { req, res -> res.send("URL is incorrect.") }
            }
        }

        express.listen(WebCTCConfig.portNumber)
        railCacheUpdate = RailCacheUpdate()
        signalCacheUpdate = SignalCacheUpdate()
    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        express.stop()
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
        const val VERSION = "0.0.2"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}