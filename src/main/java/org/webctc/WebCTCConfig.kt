package org.webctc

import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property

class WebCTCConfig {
    companion object {
        var portNumber: Int = 8080

        fun preInit(cfg: Configuration) {
            cfg.load()
            val portProperty: Property = cfg["Network", "port number", 8080]
            portProperty.comment = "Port number used by WebCTC"
            this.portNumber = portProperty.int
            cfg.save()
        }
    }
}