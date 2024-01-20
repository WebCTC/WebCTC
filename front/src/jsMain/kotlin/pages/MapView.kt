package pages

import client
import components.*
import emotion.react.Global
import emotion.react.css
import emotion.react.styles
import emotion.styled.styled
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import js.objects.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import module.panzoom
import mui.material.CssBaseline
import org.webctc.common.types.WayPoint
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.trains.FormationData
import react.*
import react.dom.html.ReactHTML.body
import react.dom.html.ReactHTML.div
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.svg
import web.cssom.*
import web.svg.SVGElement
import web.timers.clearInterval
import web.timers.setInterval
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

val interval = 3.seconds

val MapView = FC<Props> {
    val (railList, setRailList) = useState(listOf<LargeRailData>())
    var signalList by useState(listOf<SignalData>())
    var formationList by useState(listOf<FormationData>())
    var waypointList by useState(listOf<WayPoint>())

    val mtxRef = useRef<SVGElement>()

    var scale by useState(1.0)

    val fetchRails = {
        MainScope().launch {
            val railList: List<LargeRailData> = client.get("/api/rails/").body()
            setRailList(railList)
        }
    }

    val updateSignals = {
        MainScope().launch {
            signalList = client.get("/api/signals/").body()
        }
    }

    val updateFormations = {
        MainScope().launch {
            formationList = client.get("/api/formations/").body()
        }
    }

    val fetchWayPoints = {
        MainScope().launch {
            waypointList = client.get("/api/waypoints/").body()
        }
    }

    useEffectOnce {
        val mtx = mtxRef.current!!
        val instance = panzoom(mtx, jso { smoothScroll = false })
        instance.on("transform") { e: dynamic ->
            scale = min(e.getTransform().scale.toString().toDouble(), 4.0)
            return@on {}
        }

        fetchRails()
        updateSignals()
        updateFormations()
        fetchWayPoints()

        setInterval(interval) {
            updateSignals()
            updateFormations()
        }.let {
            cleanup {
                clearInterval(it)
            }
        }

        MainScope().launch {
            client.webSocket("/api/rails/railsocket") {
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

    div {
        css {
            height = 100.vh
            display = Display.flex
            flexDirection = FlexDirection.column
        }

        Header {}

        MapSVG {
            g {
                ref = mtxRef
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
                    waypointList.forEach {
                        WWayPoint {
                            wayPoint = it
                            this.scale = scale
                        }
                    }
                }
            }
        }
    }

    Global {
        styles {
            body {
                backgroundColor = Color("darkslategray")
            }
        }
    }
}

val MapSVG = svg.styled {
    flex = number(1.0)
    width = 100.pct
}