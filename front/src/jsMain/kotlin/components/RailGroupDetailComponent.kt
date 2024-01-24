package components

import client
import emotion.react.Global
import emotion.react.styles
import io.ktor.client.request.*
import io.ktor.http.*
import js.objects.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import mui.icons.material.ContentCopy
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup
import react.*
import react.dom.aria.ariaReadOnly
import react.dom.events.ChangeEvent
import web.cssom.*
import web.html.HTMLInputElement
import web.navigator.navigator

external interface RailGroupDetailProps : Props {
    var railGroup: RailGroup
    var selectedRails: List<PosInt>
    var deleteRailGroup: (uuid: UUID) -> Unit
}

val RailGroupDetail = FC<RailGroupDetailProps> {
    val rg = it.railGroup
    val deleteRailGroup = it.deleteRailGroup
    val selectedRails = it.selectedRails

    var name by useState(rg.name)
    var railPosList by useState(rg.railPosList.toList())
    var rsPosList by useState(rg.rsPosList.toList())
    var nextRailGroupList by useState(rg.nextRailGroupList.toList())
    var displayPosList by useState(rg.displayPosList.toList())

    var hoveringRailPos by useState<PosInt?>(null)

    val saveRailGroup = {
        rg.name = name
        rg.railPosList.apply {
            clear()
            addAll(railPosList)
        }
        rg.rsPosList.apply {
            clear()
            addAll(rsPosList)
        }
        rg.nextRailGroupList.apply {
            clear()
            addAll(nextRailGroupList)
        }
        rg.displayPosList.apply {
            clear()
            addAll(displayPosList)
        }
    }

    val sendRailGroup = {
        MainScope().launch {
            client.post("/api/railgroups/update") {
                contentType(ContentType.Application.Json)
                parameter("uuid", rg.uuid)
                setBody(rg)
            }
        }
    }

    Box {
        sx {
            paddingInline = 16.px
            overflowY = Auto.auto
        }

        Box {
            sx { paddingBlock = 8.px }
            +"Name"
            Card {
                OutlinedInput {
                    fullWidth = true
                    value = name
                    onChange = {
                        val event = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        val target = event.target
                        name = target.value
                    }
                }
            }
        }

        Box {
            sx { paddingBlock = 8.px }
            +"UUID"
            Card {
                OutlinedInput {
                    fullWidth = true
                    value = rg.uuid
                    inputProps = jso { ariaReadOnly = true }
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        IconButton {
                            ContentCopy {}
                            onClick = { navigator.clipboard.writeText(rg.uuid.toString()) }
                        }
                    }
                }
            }
        }

        Box {
            sx { paddingBlock = 8.px }
            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                +"Rails"
                Button {
                    +"Add"
                    variant = ButtonVariant.outlined
                    onClick = {
                        railPosList = railPosList.toMutableList().apply {
                            addAll(selectedRails)
                        }
                    }
                }
            }
            Card {
                List {
                    disablePadding = true
                    dense = true
                    railPosList.forEach { pos ->
                        ListItem {
                            ListItemText {
                                primary = ReactNode("${pos.x}, ${pos.y}, ${pos.z}")
                            }
                            secondaryAction = IconButton.create {
                                Delete {}
                                onClick = {
                                    railPosList = railPosList.toMutableList().apply {
                                        remove(pos)
                                    }
                                }
                            }
                            onMouseEnter = { hoveringRailPos = pos }
                            onMouseLeave = { hoveringRailPos = null }
                        }
                    }
                }
            }
        }
        Box {
            sx {
                paddingBlock = 8.px
            }
            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                +"RedStone Pos"
                Box {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.flexEnd
                    }
                    Button {
                        +"Receive"
                        variant = ButtonVariant.outlined
                        onClick = {
                        }
                    }
                    Button {
                        sx {
                            marginInlineStart = 8.px
                        }
                        +"Add"
                        variant = ButtonVariant.outlined
                        onClick = {
                            rsPosList = rsPosList.toMutableList().apply {
                            }
                        }
                    }
                }
            }
        }
        Box {
            sx {
                paddingBlock = 8.px
            }
            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                +"Display Pos"
                Box {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.flexEnd
                    }
                    Button {
                        +"Receive"
                        variant = ButtonVariant.outlined
                        onClick = {
                        }
                    }
                    Button {
                        sx {
                            marginInlineStart = 8.px
                        }
                        +"Add"
                        variant = ButtonVariant.outlined
                        onClick = {
                            displayPosList = displayPosList.toMutableList().apply {
                            }
                        }
                    }
                }
            }
        }

        Box {
            sx {
                paddingBlock = 8.px
            }
            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                +"Next RailGroups"
                Button {
                    +"Add"
                    variant = ButtonVariant.outlined
                    onClick = {
                        nextRailGroupList = nextRailGroupList.toMutableList().apply {
                        }
                    }
                }
            }


        }


        Box {
            sx {
                paddingBlock = 8.px
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
            }
            Button {
                +"Save"
                onClick = {
                    saveRailGroup()
                    sendRailGroup()
                }
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
            railPosList.forEach {
                "g#rail\\,${it.x}\\,${it.y}\\,${it.z}" {
                    set(CustomPropertyName("stroke"), "orange")
                }
            }
        }
    }
}