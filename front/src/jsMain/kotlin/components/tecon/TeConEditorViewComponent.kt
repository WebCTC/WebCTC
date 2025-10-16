package components.tecon

import client
import components.common.DeleteButtonWithDialog
import components.common.OutlinedInputWithLabel
import components.tecon.editor.*
import io.ktor.client.request.*
import io.ktor.http.*
import js.uri.encodeURIComponent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import mui.icons.material.Download
import mui.material.*
import mui.material.Size
import mui.system.sx
import org.webctc.common.types.PosInt2D
import org.webctc.common.types.kotlinxJson
import org.webctc.common.types.tecon.TeCon
import org.webctc.common.types.tecon.shape.IShape
import react.FC
import react.Props
import react.router.useNavigate
import react.useRef
import react.useState
import utils.removeAtNew
import utils.setNew
import web.cssom.*
import web.dom.document

external interface TeConEditorViewComponentProps : Props {
    var tecon: TeCon
}

val TeConEditorViewComponent = FC<TeConEditorViewComponentProps> { props ->
    val tecon = props.tecon

    var name by useState(tecon.name)
    var parts by useState(tecon.parts)

    var dotVisible by useState(true)
    val panzoomRef = useRef<dynamic>()

    var mode by useState<EditMode>(EditMode.HAND)

    var nowMousePos by useState(PosInt2D(0, 0))
    var selectedPosList by useState<List<PosInt2D>>(listOf())
    var selectedPart by useState<IShape?>(null)

    var sending by useState(false)

    val navigate = useNavigate()

    Box {
        sx {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        SvgWithDot {
            dotVisibility = dotVisible
            cursorVisibility = mode.posCount > 0
            onClick = { _ ->
                if (mode.posCount > 0) {
                    val isInfinitySelection = mode.isInfinitySelection()

                    var newSelectedPosList =
                        if (nowMousePos in selectedPosList && !isInfinitySelection) selectedPosList - nowMousePos
                        else selectedPosList + nowMousePos.copy()

                    val fin = selectedPosList.lastOrNull() == nowMousePos && isInfinitySelection
                    if (fin) newSelectedPosList = newSelectedPosList.dropLast(1)

                    if (newSelectedPosList.size == mode.posCount || fin) {
                        parts += mode.createIShape(newSelectedPosList)!!
                        newSelectedPosList = listOf()
                    }

                    selectedPosList = newSelectedPosList
                }
            }
            onUpdateMousePos = { nowMousePos = it }
            onInitPanzoom = { panzoomRef.current = it }

            parts.forEachIndexed { index, iShape ->
                val onSelect = { if (selectedPart == iShape) selectedPart = null else selectedPart = iShape }
                val onDelete = { parts = parts.removeAtNew(index) }
                val selected = selectedPart == iShape
                +EditMode.createElement(iShape, mode, onSelect, onDelete, selected)
            }

            if (selectedPosList.isNotEmpty()) {
                +EditMode.createPreviewElement(mode, selectedPosList + nowMousePos)
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
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = 16.px
                }
                Card {
                    sx {
                        backgroundColor = Color("white")
                        height = 100.pct
                        overflow = Auto.auto
                    }
                    CardContent {
                        BoxSettings {
                            panzoom = panzoomRef.current
                            this.name = name
                            onChangeName = { name = it }
                            setDotVisibility = { dotVisible = it }
                            onChangeEditMode = {
                                if (it == EditMode.HAND) {
                                    panzoomRef.current?.resume()
                                } else {
                                    panzoomRef.current?.pause()
                                }
                                mode = it
                                selectedPosList = listOf()
                            }
                            canSave = (name != tecon.name || parts != tecon.parts) && !sending
                            onSave = {
                                sending = true

                                val newTeCon = tecon.copy(name = name, parts = parts)
                                MainScope().launch {
                                    client.put("/api/tecons/${tecon.uuid}") {
                                        contentType(ContentType.Application.Json)
                                        setBody(newTeCon)
                                    }
                                    tecon.updateBy(newTeCon)
                                    sending = false
                                }
                            }
                            onDelete = {
                                MainScope().launch {
                                    client.delete("/api/tecons/${tecon.uuid}")
                                    navigate("/p/tecons")
                                }
                            }
                            onDownload = {
                                val data = "data:application/json;charset=utf-8," + encodeURIComponent(
                                    kotlinxJson.encodeToString(tecon)
                                )
                                val link = document.createElement("a")
                                link.setAttribute("href", data)
                                link.setAttribute("download", "${tecon.name}.json")
                                link.click()
                            }
                        }
                    }
                }

                selectedPart?.let { part ->
                    Card {
                        sx {
                            backgroundColor = Color("white")
                            height = 100.pct
                            overflow = Auto.auto
                        }
                        CardContent {
                            +EditMode.createPropertyElement(part) {
                                val index = parts.indexOf(part)
                                selectedPart = it
                                parts = parts.setNew(index, it)
                            }
                        }
                    }
                }
            }
        }
    }
}

external interface BoxSettingsProps : Props {
    var panzoom: dynamic
    var name: String
    var onChangeName: (String) -> Unit
    var setDotVisibility: (Boolean) -> Unit
    var onChangeEditMode: (EditMode) -> Unit
    var canSave: Boolean
    var onSave: () -> Unit
    var onDelete: () -> Unit
    var onDownload: () -> Unit
}

private val BoxSettings = FC<BoxSettingsProps> { props ->
    val panzoom = props.panzoom
    val name = props.name
    val onChangeName = props.onChangeName
    val setDotVisibility = props.setDotVisibility
    val onChangeEditMode = props.onChangeEditMode
    val canSave = props.canSave
    val onSave = props.onSave
    val onDelete = props.onDelete
    val onDownload = props.onDownload

    Typography {
        sx {
            fontSize = FontSize.larger
            fontWeight = FontWeight.bold
        }
        +"Editor"
    }
    Box {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 16.px
        }

        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.flexEnd
                gap = 8.px
            }

            IconButton {
                size = Size.small
                disabled = canSave
                onClick = { onDownload() }
                title = "保存"
                Download {}
            }
        }

        OutlinedInputWithLabel {
            this.name = name
            this.onChange = onChangeName
        }

        Box {
            sx {
                display = Display.flex
                gap = 8.px
            }
            ToggleButtonHome { this.panzoom = panzoom }

            ToggleButtonGroupZoom { this.panzoom = panzoom }

            ToggleButtonGroupVisibility { onChange = setDotVisibility }
        }
        ToggleButtonGroupEditMode {
            onChange = onChangeEditMode
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
            }
            Button {
                +"保存"
                variant = ButtonVariant.contained
                disabled = !canSave
                onClick = { onSave() }
            }
            DeleteButtonWithDialog {
                this.onDelete = { onDelete() }
                title = "TeConの削除"
                message = "TeCon: $name を削除しますか？"
            }
        }
    }
}