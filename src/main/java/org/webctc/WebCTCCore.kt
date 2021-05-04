package org.webctc

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import express.Express
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.config.Configuration
import org.webctc.cache.rail.RailCacheUpdateThread
import org.webctc.cache.signal.SignalCacheUpdateThread
import org.webctc.router.DefaultRouter
import org.webctc.router.api.*

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var express: Express
    lateinit var railData: WorldSavedData
    lateinit var signalData: WorldSavedData
    lateinit var railCacheUpdateThread: Thread
    lateinit var signalCacheUpdateThread: Thread

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        WebCTCConfig.preInit(Configuration(event.suggestedConfigurationFile))
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
    }

    @Mod.EventHandler
    fun onServerStart(event: FMLServerStartedEvent) {
        server = MinecraftServer.getServer()

        express = object : Express() {
            init {
                use("/", DefaultRouter())
                use("/api", ApiRouter())
                use("/api/formations", FormationsRouter())
                use("/api/trains", TrainsRouter())
                use("/api/rails", RailRouter())
                use("/api/signals", SignalRouter())
                all() { req, res -> res.send("URL is incorrect.") }

                listen(WebCTCConfig.portNumber)
            }
        }
        railCacheUpdateThread = RailCacheUpdateThread()
        railCacheUpdateThread.start()
        signalCacheUpdateThread = SignalCacheUpdateThread()
        signalCacheUpdateThread.start()
    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        express.stop()
//        railData.markDirty()
//        signalData.markDirty()
        railCacheUpdateThread.interrupt()
        signalCacheUpdateThread.interrupt()
    }

    companion object {
        const val MODID = "webctc"
        const val VERSION = "1.0-SNAPSHOT"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}