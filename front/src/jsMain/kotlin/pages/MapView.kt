package pages

import components.*
import emotion.react.Global
import emotion.react.styles
import emotion.styled.styled
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import js.objects.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import mui.material.CssBaseline
import org.webctc.common.types.WayPoint
import org.webctc.common.types.rail.IRailMapData
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.rail.RailMapData
import org.webctc.common.types.rail.RailMapSwitchData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.trains.FormationData
import panzoom
import react.FC
import react.Props
import react.dom.html.ReactHTML.body
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.svg
import react.useEffectOnce
import react.useState
import web.cssom.*
import web.dom.document
import web.timers.setInterval
import kotlin.time.Duration.Companion.seconds

val client = HttpClient(Js) {
    val jsonPreset = Json {
        serializersModule = SerializersModule {
            polymorphic(IRailMapData::class) {
                subclass(RailMapData::class)
                subclass(RailMapSwitchData::class)
            }
        }
        ignoreUnknownKeys = true
    }

    install(ContentNegotiation) {
        json(jsonPreset)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(jsonPreset)
    }
}

val MapView = FC<Props> {
    var (railList, setRailList) = useState(mutableListOf<LargeRailData>())
    var signalList by useState(mutableListOf<SignalData>())
    var formationList by useState(mutableListOf<FormationData>())
    var waypointList by useState(mutableListOf<WayPoint>())

    useEffectOnce {
        val map = document.getElementById("mtx")!!
        panzoom(map, jso { smoothScroll = false })

        val port = 8080
        val interval = 3.seconds

        MainScope().launch {
            val resList = client.get("/api/rails/") {
                this.port = port
                accept(ContentType.Application.Json)
            }.body<MutableList<LargeRailData>>()

            setRailList { resList }
        }

        setInterval(interval) {
            MainScope().launch {
                signalList = client.get("/api/signals/") {
                    this.port = port
                    accept(ContentType.Application.Json)
                }.body()
            }
            MainScope().launch {
                formationList = client.get("/api/formations/") {
                    this.port = port
                    accept(ContentType.Application.Json)
                }.body()
            }
        }

        MainScope().launch {
            waypointList = client.get("/api/waypoints/") {
                accept(ContentType.Application.Json)
            }.body()
        }

        MainScope().launch {
            client.webSocket(path = "/api/rails/railsocket", port = port) {
                while (true) {
                    val updatedRails = receiveDeserialized<List<LargeRailData>>()

                    setRailList {
                        it.filterNot { rail -> updatedRails.any { it.pos.contentEquals(rail.pos) } }
                            .toMutableList()
                            .apply { addAll(updatedRails) }
                    }
                }
            }
        }
    }
    CssBaseline {}

    Header {}

    MapSVG {
        g {
            id = "mtx"
            g {
                stroke = "white"
                railList.forEach { WRail { largeRailData = it } }
            }
            g {
                stroke = "lightgray"
                strokeWidth = 0.5
                signalList.groupBy { "${it.pos.x},${it.pos.z}-${it.rotation}" }
                    .forEach { (key, signals) -> WSignalGroup { this.signals = signals } }
            }
            g {
                formationList.filter { it.controlCar != null }
                    .forEach { WFormation { formation = it } }
            }
            g {
                waypointList.forEach { WWayPoint { wayPoint = it } }
            }
        }
    }

    Global {
        styles {
            body {
                height = 100.vh
                backgroundColor = Color("darkslategray")
                overflowY = Overflow.hidden
            }
        }
    }
}

val MapSVG = svg.styled {
    height = 100.vh - 80.px
    width = 100.pct
}