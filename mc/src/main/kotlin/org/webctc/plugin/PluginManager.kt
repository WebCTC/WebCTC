package org.webctc.plugin

import io.ktor.server.application.*

class PluginManager {
    companion object {
        val pluginList = mutableListOf<Application.() -> Unit>()

        fun registerPlugin(module: Application.() -> Unit) {
            pluginList.add(module)
        }
    }
}
