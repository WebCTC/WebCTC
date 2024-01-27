package pages

import components.Header
import components.map.*
import emotion.styled.styled
import js.objects.jso
import kotlinx.browser.window
import module.panzoom
import mui.material.Box
import mui.material.CssBaseline
import mui.material.List
import mui.material.Paper
import mui.system.sx
import org.webctc.common.types.WayPoint
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.trains.FormationData
import react.*
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.svg
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

    var panzoomInstance by useState<dynamic?>(null)

    var targetingFormationId by useState<Long?>(null)

    val targetingFormation = useMemo(formationList, targetingFormationId) {
        formationList.find { it.id == targetingFormationId }
    }

    val mtxRef = useRef<SVGElement>()

    var scale by useState(1.0)

    useEffectOnce {
        val mtx = mtxRef.current!!
        val panzoom = panzoom(mtx, jso { smoothScroll = false })
        panzoom.on("transform") { e: dynamic ->
            scale = e.getTransform().scale.toString().toDouble()
            return@on {}
        }
        panzoomInstance = panzoom
        return@useEffectOnce
    }

    useEffect(targetingFormation) {
        val pos = targetingFormation?.controlCar?.pos ?: return@useEffect
        panzoomInstance?.smoothMoveTo(
            -pos.x * scale + window.innerWidth / 2,
            -pos.z * scale + (window.innerHeight - 66.0) / 2
        )
        return@useEffect
    }

    CssBaseline {}

    Box {
        sx {
            height = 100.vh
            display = Display.flex
            flexDirection = FlexDirection.column
        }

        Header {}

        Box {
            sx {
                flex = number(1.0)
                display = Display.flex
                flexDirection = FlexDirection.rowReverse
            }
            Box {
                sx {
                    height = 100.pct
                    position = Position.relative
                    display = Display.flex
                    flexDirection = FlexDirection.rowReverse
                }

                Box {
                    sx {
                        position = Position.absolute
                        width = 30.vw
                        height = 100.pct
                        borderRadius = 16.px
                        padding = 16.px
                    }
                    Paper {
                        sx {
                            backgroundColor = Color("rgba(255,255,255,0.4)")
                            height = 100.pct
                            overflow = Auto.auto
                        }
                        List {
                            disablePadding = true
                            formationList.forEach { formation ->
                                ListItemFormation {
                                    this.formation = formation
                                    this.waypointList = waypointList
                                    this.onClick = {
                                        targetingFormationId = if (targetingFormationId == it.id) null else it.id
                                    }
                                    this.selected = targetingFormationId == formation.id
                                }
                            }
                        }
                    }
                }
            }

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
                            .forEach { (_, signals) -> WSignalGroup { this.signals = signals } }
                    }
                    g {
                        formationList.filter { it.controlCar != null }
                            .forEach { WFormation { formation = it } }
                    }
                    g {
                        waypointList.forEach {
                            WWayPoint {
                                wayPoint = it
                                this.scale = min(scale, 4.0)
                            }
                        }
                    }
                }
            }
        }
    }
}

val MapSVG = svg.styled {
    backgroundColor = Color("darkslategray")
}