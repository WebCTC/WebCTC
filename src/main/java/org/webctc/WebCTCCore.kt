package org.webctc

import express.Express
import net.minecraft.server.MinecraftServer
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.*
import org.webctc.railcache.RailCacheData
import org.webctc.router.DefaultRouter
import org.webctc.router.api.*
import org.webctc.thread.UpdateThread

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var express: Express
    lateinit var railData: WorldSavedData

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
        server = FMLCommonHandler.instance().minecraftServerInstance
        val world = server.entityWorld

        var railData = world.mapStorage?.getOrLoadData(RailCacheData::class.java, "webctc_railcache")
        if (railData == null) {
            railData = RailCacheData("webctc_railcache")
            world.mapStorage?.setData("webctc_railcache", railData)
        }
        this.railData = railData

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
        UpdateThread.start()
    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        express.stop()
        UpdateThread.stop()
    }

    companion object {
        const val MODID = "webctc"
        const val VERSION = "1.0-SNAPSHOT"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}