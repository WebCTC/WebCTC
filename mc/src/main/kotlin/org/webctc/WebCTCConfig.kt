package org.webctc

import jp.ngt.ngtlib.util.NGTUtil
import net.minecraftforge.common.config.Configuration
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class WebCTCConfig {
    companion object {
        private val ip: String

        init {
            val url = URL("http://checkip.amazonaws.com")
            ip = BufferedReader(InputStreamReader(url.openStream())).use { it.readLine() }
        }

        var portNumber = 8080
        var accessUrl = ""
        var accessPort: Int? = 0

        fun preInit(cfg: Configuration) {
            cfg.load()
            val portProperty = cfg["Network", "port number", 8080]
            portProperty.comment = "Port number used by WebCTC"
            val accessUrlProperty = cfg["Network", "access url", ""]
            accessUrlProperty.comment = "eg. http(s)://example.com. if null, display your machine GLOBAL IP address."
            val accessPortProperty = cfg["Network", "access port", 0]
            accessPortProperty.comment =
                "if 0, display webctc port number, else display this number"
            this.portNumber = portProperty.int
            this.accessUrl = accessUrlProperty.string.ifEmpty { "http://${if (NGTUtil.isSMP()) "localhost" else ip}" }
            this.accessPort = accessPortProperty.int.let {
                if (it == 0) portNumber
                else it
            }
            cfg.save()
        }
    }
}