package pages

import components.Header
import components.map.MapPanzoomSvg
import components.map.WRail
import components.map.WSignalGroup
import components.map.WWayPoint
import components.waypoint.AccordionWayPoint
import mui.material.Box
import mui.material.CssBaseline
import mui.material.Paper
import mui.system.sx
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.waypoint.WayPoint
import react.FC
import react.dom.svg.ReactSVG.g
import utils.useListData
import web.cssom.*

val WayPointEditor = FC {
    val railList by useListData<LargeRailData>("/api/rails/")
    val signalList by useListData<SignalData>("/api/signals/")
    var waypointList by useListData<WayPoint>("/api/waypoints/")

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
                        waypointList.forEach {
                            AccordionWayPoint {
                                waypoint = it
                                updateWayPoint = {
                                    waypointList
                                        .indexOfFirst { w -> w.identifyName == it.identifyName }
                                        .takeIf { it != -1 }
                                        ?.let { index -> waypointList.toMutableList().apply { set(index, it) } }
                                        ?.let { waypointList = it }
                                }
                                deleteWayPoint = { id ->
                                    waypointList
                                        .indexOfFirst { w -> w.identifyName == id }
                                        .takeIf { it != -1 }
                                        ?.let { waypointList.toMutableList().apply { removeAt(it) } }
                                        ?.let { waypointList = it }
                                }
                            }
                        }
                    }
                }
            }
            MapPanzoomSvg {
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
                g { waypointList.forEach { WWayPoint { wayPoint = it } } }
            }
        }
    }
}