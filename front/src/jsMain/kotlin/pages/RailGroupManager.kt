package pages

import client
import components.Header
import components.map.MapPanzoomSvg
import components.map.WRailHover
import components.map.WSignalGroup
import components.map.WWayPoint
import components.railgroup.RailGroupDetail
import emotion.react.Global
import emotion.react.styles
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.waypoint.WayPoint
import react.FC
import react.ReactNode
import react.dom.html.ReactHTML.h1
import react.dom.svg.ReactSVG.g
import react.useEffectOnce
import react.useState
import utils.useListData
import web.cssom.*


val RailGroupManager = FC {
    val railList by useListData<LargeRailData>("/api/rails/")
    val signalList by useListData<SignalData>("/api/signals/")
    val waypointList by useListData<WayPoint>("/api/waypoints/")
    val (railGroups, setRailGroups) = useListData<RailGroup>("/api/railgroups/")
    var selectedRails by useState<Collection<PosInt>>(setOf())

    var isShiftKeyDown by useState(false)

    var activeRailGroupUUID by useState<UUID?>(null)

    val activeRailGroup = railGroups.find { it.uuid == activeRailGroupUUID }

    val createRailGroup = {
        MainScope().launch {
            val railGroup: RailGroup = client.post("/api/railgroups/create").body()
            setRailGroups {
                it.toMutableList().apply {
                    add(railGroup)
                }
            }
        }
    }

    val deleteRailGroup = { uuid: UUID ->
        MainScope().launch {
            client.post("/api/railgroups/delete") {
                parameter("uuid", uuid)
            }
            setRailGroups {
                it.toMutableList().apply {
                    removeAll { it.uuid == uuid }
                }
            }
        }
    }

    useEffectOnce {
        window.onkeydown = { if (it.key == "Shift") isShiftKeyDown = true }
        window.onkeyup = { if (it.key == "Shift") isShiftKeyDown = false }

        cleanup {
            window.onkeydown = null
            window.onkeyup = null
        }
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
                display = Display.flex
                flex = number(1.0)
                overflowY = Auto.auto
            }
            MapPanzoomSvg {
                g {
                    stroke = "white"
                    railList.forEach {
                        WRailHover {
                            largeRailData = it
                            onClick = {
                                selectedRails = selectedRails.toMutableSet().apply {
                                    if (size == 1 && first() == it.pos) {
                                        clear()
                                    } else {
                                        if (!isShiftKeyDown) clear()
                                        add(it.pos)
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
                g {
                    waypointList.forEach {
                        WWayPoint {
                            wayPoint = it
                        }
                    }
                }
            }
            Box {
                sx {
                    background = Color("whitesmoke")
                    paddingInline = 16.px
                    display = Display.flex
                    flexDirection = FlexDirection.column
                }
                h1 {
                    +"RailGroups"
                }

                Box {
                    Button {
                        +"Create"
                        onClick = { createRailGroup() }
                        variant = ButtonVariant.contained
                    }
                }
                Paper {
                    sx {
                        flex = number(1.0)
                        marginBlock = 8.px
                        overflowY = Auto.auto
                    }
                    List {
                        dense = true
                        disablePadding = true
                        railGroups.sortedBy { it.name }
                            .forEach { rg ->
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
            Box {
                sx {
                    width = 25.pct
                    background = Color("silver")
                    paddingInline = 16.px
                    overflowY = Auto.auto
                }
                h1 {
                    +"Detail"
                }
                activeRailGroup?.let {
                    RailGroupDetail {
                        key = it.uuid.toString()
                        this.railGroup = it
                        this.deleteRailGroup = { deleteRailGroup(it) }
                        this.selectedRails = selectedRails
                    }
                }
            }
        }
    }

    Global {
        styles {
            selectedRails.forEach {
                "g#rail\\,${it.x}\\,${it.y}\\,${it.z}" {
                    set(CustomPropertyName("stroke"), "lightblue")
                }
            }
        }
    }
}