package components.railgroup

import client
import components.railgroup.detail.*
import emotion.react.Global
import emotion.react.styles
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup
import react.FC
import react.Props
import react.create
import react.useState
import web.cssom.*

external interface RailGroupDetailProps : Props {
    var railGroup: RailGroup
    var selectedRails: Collection<PosInt>
    var deleteRailGroup: (uuid: UUID) -> Unit
}

val RailGroupDetail = FC<RailGroupDetailProps> { props ->
    val rg = props.railGroup
    val deleteRailGroup = props.deleteRailGroup
    val selectedRails = props.selectedRails

    var name by useState(rg.name)
    var rails by useState(rg.railPosList)
    var rsList by useState(rg.rsPosList)
    var displayList by useState(rg.displayPosList)
    var nextRailGroups by useState(rg.nextRailGroupList)
    var switchSetting by useState(rg.switchSetting)
    var hoveringRail by useState<PosInt?>(null)

    var sending by useState(false)

    val sendRailGroup = {
        sending = true

        val changedRailGroup = RailGroup(
            rg.uuid,
            name,
            rails,
            rsList,
            nextRailGroups,
            displayList,
            switchSetting
        )

        MainScope().launch {
            client.put("/api/railgroups/${rg.uuid}") {
                contentType(ContentType.Application.Json)
                parameter("uuid", rg.uuid)
                setBody(changedRailGroup)
            }

            rg.updateBy(changedRailGroup)
            sending = false
        }
    }

    Box {
        sx {
            paddingInline = 16.px
            paddingBottom = 16.px
            overflowY = Auto.auto
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 16.px
        }

        BoxRgName {
            this.name = name
            this.onChange = { name = it }
        }

        BoxRgUUID { uuid = rg.uuid }

        BoxRailList {
            this.rails = rails
            this.updateRails = { rails = it }
            this.selectedRails = selectedRails
            this.hoveringRail = hoveringRail
            this.setHoveringRail = { hoveringRail = it }
        }

        BoxPosIntWithKeyList {
            title = "RedStone Pos"
            posList = rsList
            updatePosList = { rsList = it }
            wsPath = "/api/railgroups/ws/block"
        }

        BoxPosIntList {
            title = "Display Pos"
            posList = displayList
            updatePosList = { displayList = it }
            wsPath = "/api/railgroups/ws/signal"
        }

        Box {
            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                    paddingBottom = 8.px
                }
                +"Next RailGroups"
                Button {
                    +"Add"
                    variant = ButtonVariant.outlined
                    onClick = {
                        nextRailGroups = nextRailGroups.toMutableSet().apply {
                        }
                    }
                    disabled = true
                }
            }
            Paper {
                List {
                    disablePadding = true
                    nextRailGroups.forEach { uuid ->
                        ListItem {
                            secondaryAction = IconButton.create {
                                Delete {}
                                onClick = { nextRailGroups = nextRailGroups - uuid }
                            }

                            ListItemText {
                                +uuid.toString()
                            }
                        }
                    }
                }
            }
        }

        BoxSwitchSetting {
            this.switchSetting = switchSetting
            updateSwitchSetting = { switchSetting = it }
        }

        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
            }
            Button {
                +"Save"
                onClick = { sendRailGroup() }
                disabled = rg.name == name &&
                        rg.railPosList == rails &&
                        rg.rsPosList == rsList &&
                        rg.nextRailGroupList == nextRailGroups &&
                        rg.displayPosList == displayList &&
                        rg.switchSetting == switchSetting || sending
                variant = ButtonVariant.contained
            }

            Button {
                +"Delete"
                onClick = { deleteRailGroup(rg.uuid) }
                color = ButtonColor.error
                variant = ButtonVariant.outlined
            }
        }
    }

    Global {
        styles {
            rails.forEach {
                "g#rail\\,${it.x}\\,${it.y}\\,${it.z}" {
                    set(CustomPropertyName("stroke"), "orange")
                }
            }
            hoveringRail?.let {
                "g#rail\\,${it.x}\\,${it.y}\\,${it.z}" {
                    set(CustomPropertyName("stroke"), "green")
                }
            }
        }
    }
}