package org.webctc.router

import express.ExpressRouter
import express.utils.MediaType
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import org.webctc.WebCTCCore

class DefaultRouter : ExpressRouter() {
    init {
        get("/") { req, res ->
            val inputStream = NGTFileLoader.getInputStream(ResourceLocation(WebCTCCore.MODID, "html/index.html"))
            res.streamFrom(0, inputStream, MediaType._html)
        }
    }
}