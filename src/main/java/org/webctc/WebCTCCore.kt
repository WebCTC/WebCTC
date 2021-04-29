package org.webctc

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import express.Express
import net.minecraft.server.MinecraftServer

@Mod(modid = WebCTCCore.MODID, version = WebCTCCore.VERSION, name = WebCTCCore.MODID)
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
                get("/") { req, res ->
                    res.send(
                        "Hello World!"
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