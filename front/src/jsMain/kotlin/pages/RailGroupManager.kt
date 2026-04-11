package pages

import client
import components.Header
import components.map.MapPanzoomSvg
import components.map.WRailHover
import components.map.WSignalGroup
import components.map.WWayPoint
import components.railgroup.CreateSplitButton
import components.railgroup.RailGroupDetail
import components.railgroup.RailGroupNodeComponent
import components.railgroup.RailGroupTreeView
import emotion.react.Global
import emotion.react.styles
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import js.coroutines.awaitCancellation
import kotlinx.browser.window
import mui.material.*
import mui.material.Size
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupFolder
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.waypoint.WayPoint
import react.*
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.h1
import react.dom.onChange
import react.dom.svg.ReactSVG.g
import utils.useAction
import utils.useListData
import web.cssom.*
import web.html.HTMLInputElement
import kotlin.uuid.Uuid

val RailGroupManager = FC {
    val railList by useListData<LargeRailData>("/api/rails")
    val signalList by useListData<SignalData>("/api/signals")
    val waypointList by useListData<WayPoint>("/api/waypoints")
    val (railGroups, setRailGroups) = useListData<RailGroup>("/api/railgroups")
    val (folders, setFolders) = useListData<RailGroupFolder>("/api/railgroups/folders")
    var searchText by useState("")
    val searchResult = useMemo(searchText, railGroups) {
        railGroups.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    var selectedRails by useState<Set<PosInt>>(setOf())

    var isShiftKeyDown by useState(false)

    var activeRailGroupUUID by useState<Uuid?>(null)

    val activeRailGroup = useMemo(
        activeRailGroupUUID, railGroups
    ) { railGroups.find { it.uuid == activeRailGroupUUID } }

    val createRailGroup = useAction {
        val railGroup: RailGroup = client.post("/api/railgroups").body()
        setRailGroups { it.toMutableList().apply { add(railGroup) } }
        activeRailGroupUUID = railGroup.uuid
    }

    val deleteRailGroup = useAction<Uuid> { uuid ->
        client.delete("/api/railgroups/$uuid")
        setRailGroups { it.toMutableList().apply { removeAll { it.uuid == uuid } } }
    }

    val createFolder = useAction<Uuid?> { parentUuid ->
        val folder: RailGroupFolder = client.post("/api/railgroups/folders").body()
        if (parentUuid != null) {
            val updated = folder.copy(parentUuid = parentUuid)
            client.put("/api/railgroups/folders/${folder.uuid}") {
                contentType(ContentType.Application.Json)
                setBody(updated)
            }
            setFolders { it + updated }
        } else {
            setFolders { it + folder }
        }
    }

    val renameFolder = useAction<RailGroupFolder, String> { folder, newName ->
        val updated = folder.copy(name = newName)
        client.put("/api/railgroups/folders/${folder.uuid}") {
            contentType(ContentType.Application.Json)
            setBody(updated)
        }
        setFolders {
            it.toMutableList().apply {
                val idx = indexOfFirst { f -> f.uuid == folder.uuid }
                if (idx >= 0) set(idx, updated)
            }
        }
    }

    val deleteFolder = useAction<RailGroupFolder> { folder ->
        client.delete("/api/railgroups/folders/${folder.uuid}")
        setFolders {
            it.toMutableList().apply {
                forEach { f -> if (f.parentUuid == folder.uuid) f.parentUuid = folder.parentUuid }
                removeAll { f -> f.uuid == folder.uuid }
            }
        }
        setRailGroups {
            it.map { rg ->
                if (rg.folderUuid == folder.uuid) rg.also { it.folderUuid = folder.parentUuid }
                else rg
            }
        }
    }

    val onSave = { _: RailGroup ->
        setRailGroups { it.toMutableList() }
    }

    useLayoutEffectOnce {
        window.onkeydown = { if (it.key == "Shift") isShiftKeyDown = true }
        window.onkeyup = { if (it.key == "Shift") isShiftKeyDown = false }

        awaitCancellation {
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
                                        if (it.pos in this) remove(it.pos) else add(it.pos)
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
                    paddingBottom = 16.px
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = 8.px
                }
                h1 {
                    +"RailGroups"
                }

                Box {
                    TextField {
                        label = ReactNode("Search Box")
                        fullWidth = true
                        size = Size.small
                        value = searchText
                        this.onChange = { formEvent ->
                            val event = formEvent.unsafeCast<ChangeEvent<HTMLInputElement, HTMLInputElement>>()
                            val target = event.target
                            val value = target.value
                            searchText = value
                        }
                    }
                }
                Box {
                    CreateSplitButton {
                        onCreateRailGroup = { createRailGroup() }
                        onCreateFolder = { createFolder(null) }
                    }
                }
                Paper {
                    sx {
                        flex = number(1.0)
                        overflowY = Auto.auto
                    }
                    if (searchText.isEmpty()) {
                        RailGroupTreeView {
                            this.folders = folders
                            this.railGroups = railGroups
                            this.activeUUID = activeRailGroupUUID
                            this.onSelectRailGroup = { activeRailGroupUUID = it }
                            this.onCreateFolder = { parentUuid -> createFolder(parentUuid) }
                            this.onRenameFolder = { folder, newName -> renameFolder(folder, newName) }
                            this.onDeleteFolder = { folder -> deleteFolder(folder) }
                        }
                    } else {
                        List {
                            dense = true
                            disablePadding = true
                            searchResult.sortedBy { it.name }.forEach { rg ->
                                RailGroupNodeComponent {
                                    selected = rg.uuid == activeRailGroupUUID
                                    onClick = { activeRailGroupUUID = rg.uuid }
                                    name = rg.name
                                    count = rg.railPosList.size
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
                        key = Key(it.uuid.toString())
                        this.railGroup = it
                        this.deleteRailGroup = { deleteRailGroup(it) }
                        this.selectedRails = selectedRails
                        this.folders = folders
                        this.onSave = onSave
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