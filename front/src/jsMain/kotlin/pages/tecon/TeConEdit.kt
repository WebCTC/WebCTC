package pages.tecon

import components.Header
import components.map.MapPanzoomSvg
import components.map.WRailHover
import components.map.WSignalGroup
import components.tecon.TeConEditorViewComponent
import kotlinx.uuid.UUID
import mui.icons.material.ContentCopy
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.tecon.TeCon
import react.*
import react.dom.svg.ReactSVG.g
import react.router.useNavigate
import react.router.useParams
import utils.useData
import utils.useListData
import web.cssom.*
import web.navigator.navigator

val TeConEdit = FC {
    val params = useParams()
    val uuid = params["uuid"] as String
    val navigate = useNavigate()

    val tecon by useData<TeCon>("/api/tecons/$uuid") {
        navigate("/p/tecons")
    }

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
                                        activeRailGroupUUID = null
                                    } else {
                                        selectedRail = pos
                                        activeRailGroupUUID = if (railGroups.count { pos in it.railPosList } == 1) {
                                            railGroups.find { pos in it.railPosList }!!.uuid
                                        } else null
                                    }
                                }
                                color =
                                    if (activeRailGroupUUID != null && it.pos in activeRailGroup!!.railPosList) "orange" else null
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
                            elevation = 0
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
                                            elevation = 0
                                            sx { borderRadius = 0.px }
                                            ListItem {
                                                disableGutters = true
                                                disablePadding = true
                                                secondaryAction = IconButton.create {
                                                    ContentCopy {}
                                                    onClick = { navigator.clipboard.writeText(rg.uuid.toString()) }
                                                }
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
            }

            Box {
                sx { flexGrow = number(2.0) }
                if (tecon == null) {
                    Box {
                        sx {
                            height = 100.pct
                            backgroundColor = Color("#202020")
                        }
                        LinearProgress {}
                    }
                } else {
                    TeConEditorViewComponent {
                        this.tecon = tecon!!
                    }
                }
            }
        }
    }
}