package org.webctc

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import express.Express
import net.minecraft.server.MinecraftServer
import net.minecraftforge.common.config.Configuration
import org.webctc.railcache.RailCache
import org.webctc.railcache.RailCacheData
import org.webctc.router.DefaultRouter
import org.webctc.router.api.ApiRouter
import org.webctc.router.api.FormationsRouter
import org.webctc.router.api.RailRouter
import org.webctc.router.api.TrainsRouter

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID, acceptableRemoteVersions = "*")
class WebCTCCore {
    lateinit var server: MinecraftServer
    lateinit var express: Express

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
    fun onServerStart(event: FMLServerStartingEvent) {
        server = event.server
        val world = server.entityWorld

        var data = world.mapStorage.loadData(RailCache::class.java, "webctc_railcache")
        if (data == null) {
            data = RailCacheData("webctc_railcache")
            world.mapStorage.setData("webctc_railcache", data)
        }
        express = object : Express() {
            init {
                use("/", DefaultRouter())
                use("/api", ApiRouter())
                use("/api/formations", FormationsRouter())
                use("/api/trains", TrainsRouter())
                use("/api/rails", RailRouter())
                all() { req, res -> res.send("URL is incorrect.") }

                listen(WebCTCConfig.portNumber)
            }
        }
    }

    @Mod.EventHandler
    fun onServerStop(event: FMLServerStoppingEvent) {
        express.stop()
    }

    companion object {
        const val MODID = "webctc"
        const val VERSION = "1.0-SNAPSHOT"

        @Mod.Instance
        lateinit var INSTANCE: WebCTCCore
    }
}