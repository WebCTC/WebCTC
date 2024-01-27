package pages

import components.Header
import components.map.WFormation
import components.map.WRail
import components.map.WSignalGroup
import components.map.WWayPoint
import emotion.react.Global
import emotion.react.css
import emotion.react.styles
import emotion.styled.styled
import js.objects.jso
import module.panzoom
import mui.material.CssBaseline
import org.webctc.common.types.WayPoint
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.trains.FormationData
import react.FC
import react.dom.html.ReactHTML.body
import react.dom.html.ReactHTML.div
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.svg
import react.useEffectOnce
import react.useRef
import react.useState
import utils.useIntervalListData
import utils.useListData
import utils.useListDataWithWebsocket
import web.cssom.*
import web.svg.SVGElement
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

val interval = 3.seconds

val MapView = FC {
    val railList by useListDataWithWebsocket<LargeRailData>("/api/rails/", "/api/rails/railsocket") { a, b ->
        a.pos == b.pos
    }
    val signalList by useIntervalListData<SignalData>("api/signals/", interval)
    val formationList by useIntervalListData<FormationData>("api/formations/", interval)
    val waypointList by useListData<WayPoint>("api/waypoints/")

    val mtxRef = useRef<SVGElement>()

    var scale by useState(1.0)

    useEffectOnce {
        val mtx = mtxRef.current!!
        val instance = panzoom(mtx, jso { smoothScroll = false })
        instance.on("transform") { e: dynamic ->
            scale = min(e.getTransform().scale.toString().toDouble(), 4.0)
            return@on {}
        }

        return@useEffectOnce
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