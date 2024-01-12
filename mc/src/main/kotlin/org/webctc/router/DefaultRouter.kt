package org.webctc.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import org.webctc.WebCTCCore

class DefaultRouter : AbstractRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            val inputStream = NGTFileLoader.getInputStream(ResourceLocation(WebCTCCore.MODID, "html/index.html"))
            call.respondText(inputStream.bufferedReader().readText(), ContentType.Text.Html)
        }
        get("/{FileName}") {
            val fileName = call.parameters["FileName"]
            try {
                val inputStream = NGTFileLoader.getInputStream(ResourceLocation(WebCTCCore.MODID, "html/$fileName"))
                call.respondText(inputStream.bufferedReader().readText(), ContentType.Text.Html)
            } catch (e: Exception) {
                call.respondText("URL is incorrect.")
            }
        }
    }
}