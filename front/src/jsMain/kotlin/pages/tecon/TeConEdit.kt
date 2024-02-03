package pages.tecon

import components.Header
import components.map.MapPanzoomSvg
import components.map.WRailHover
import components.map.WSignalGroup
import components.tecon.TeConEditorViewComponent
import emotion.react.Global
import emotion.react.styles
import kotlinx.uuid.UUID
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.signal.SignalData
import react.FC
import react.ReactNode
import react.dom.svg.ReactSVG.g
import react.useMemo
import react.useState
import utils.useListData
import web.cssom.*

val TeConEdit = FC {
    val railList by useListData<LargeRailData>("/api/rails")
    val signalList by useListData<SignalData>("/api/signals")
    val railGroups by useListData<RailGroup>("/api/railgroups")
    var selectedRail by useState<PosInt>()
    var activeRailGroupUUID by useState<UUID>()
    val activeRailGroup = useMemo(railGroups, activeRailGroupUUID) {
        railGroups.find { it.uuid == activeRailGroupUUID }
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
                flexGrow = number(1.0)
                display = Display.flex
                flexDirection = FlexDirection.column
            }
            Box {
                sx {
                    height = 33.pct
                    display = Display.flex
                    flexDirection = FlexDirection.row
                }
                MapPanzoomSvg {
                    g {
                        stroke = "white"
                        railList.forEach {
                            WRailHover {
                                largeRailData = it
                                onClick = {
                                    val pos = it.pos
                                    if (selectedRail == pos) {
                                        selectedRail = null
                                    } else {
                                        selectedRail = pos
                                        if (railGroups.count { pos in it.railPosList } == 1) {
                                            activeRailGroupUUID = railGroups.find { pos in it.railPosList }!!.uuid
                                        }
                                    }
                                }
                            }
                        }
                    }
                    g {
                        stroke = "lightgray"
                        strokeWidth = 0.5
                        signalList.groupBy { "${it.pos.x},${it.pos.z}-${it.rotation}" }
                            .forEach { (_, signals) -> WSignalGroup { this.signals = signals } }
                    }
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
                                dense = true
                                disablePadding = true
                                railGroups
                                    .filter { selectedRail == null || selectedRail in it.railPosList }
                                    .sortedBy { it.name }
                                    .forEach { rg ->
                                        Paper {
                                            sx { backgroundColor = Color("white") }
                                            ListItemButton {
                                                selected = rg.uuid == activeRailGroupUUID
                                                onClick = { activeRailGroupUUID = rg.uuid }
                                                ListItemText {
                                                    primary = ReactNode(rg.name)
                                                    secondary = ReactNode("${rg.railPosList.size} rails")
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            Box {
                sx { flexGrow = number(2.0) }
                TeConEditorViewComponent {}
            }
        }
    }

    Global {
        styles {
            activeRailGroup?.railPosList?.forEach {
                "g#rail\\,${it.x}\\,${it.y}\\,${it.z}" {
                    set(CustomPropertyName("stroke"), "orange")
                }
            }
        }
    }
}