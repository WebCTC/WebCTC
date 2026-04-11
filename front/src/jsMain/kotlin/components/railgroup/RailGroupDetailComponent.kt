package components.railgroup

import client
import components.common.DeleteButtonWithDialog
import components.common.OutlinedInputWithLabel
import components.railgroup.detail.*
import emotion.react.Global
import emotion.react.styles
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.material.Box
import mui.material.Button
import mui.material.ButtonVariant
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupFolder
import react.FC
import react.Props
import react.useState
import web.cssom.*
import kotlin.uuid.Uuid

external interface RailGroupDetailProps : Props {
    var railGroup: RailGroup
    var selectedRails: Collection<PosInt>
    var deleteRailGroup: (uuid: Uuid) -> Unit
    var folders: List<RailGroupFolder>
    var onSave: (RailGroup) -> Unit
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
    var switchSettings by useState(rg.switchSettings)
    var folderUuid by useState(rg.folderUuid)
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
            switchSettings,
            folderUuid = folderUuid
        )

        MainScope().launch {
            client.put("/api/railgroups/${rg.uuid}") {
                contentType(ContentType.Application.Json)
                parameter("uuid", rg.uuid)
                setBody(changedRailGroup)
            }

            rg.updateBy(changedRailGroup)
            props.onSave(changedRailGroup)
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

        OutlinedInputWithLabel {
            this.name = name
            this.onChange = { name = it }
        }

        BoxRgUUID { uuid = rg.uuid }

        BoxFolderPicker {
            this.folders = props.folders
            this.currentFolderUuid = folderUuid
            this.onChange = { folderUuid = it }
        }

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

        BoxRailGroupList {
            title = "Next RailGroup"
            railGroupList = nextRailGroups
            updateRailGroupList = { nextRailGroups = it }
        }

        BoxSwitchSettings {
            this.switchSettings = switchSettings
            updateSwitchSettings = { switchSettings = it }
        }

        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
            }
            Button {
                +"保存"
                onClick = { sendRailGroup() }
                disabled = rg.name == name &&
                        rg.railPosList == rails &&
                        rg.rsPosList == rsList &&
                        rg.nextRailGroupList == nextRailGroups &&
                        rg.displayPosList == displayList &&
                        rg.switchSettings == switchSettings &&
                        rg.folderUuid == folderUuid || sending
                variant = ButtonVariant.contained
            }
            DeleteButtonWithDialog {
                title = "RailGroupの削除"
                message = "RailGroup: $name を削除しますか？"
                onDelete = { deleteRailGroup(rg.uuid) }
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