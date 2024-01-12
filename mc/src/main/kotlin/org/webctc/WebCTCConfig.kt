package org.webctc

import net.minecraftforge.common.config.Configuration

class WebCTCConfig {
    companion object {
        var portNumber = 8080
        var accessUrl = ""
        var accessPort = 0

        fun preInit(cfg: Configuration) {
            cfg.load()
            val portProperty = cfg["Network", "port number", 8080]
            portProperty.comment = "Port number used by WebCTC"
            val accessUrlProperty = cfg["Network", "access url", ""]
            accessUrlProperty.comment = "if null, display your machine IP address"
            val accessPortProperty = cfg["Network", "access port", 0]
            accessPortProperty.comment = "if 0, display webctc port number"
            this.portNumber = portProperty.int
            this.accessUrl = accessUrlProperty.string
            this.accessPort = accessPortProperty.int
            cfg.save()
        }
    }
}