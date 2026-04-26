package components.tecon.editor.element

import components.railgroup.detail.BoxPosIntList
import components.railgroup.detail.BoxRailGroupList
import emotion.react.css
import mui.icons.material.Delete
import mui.material.*
import mui.material.Size
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import org.webctc.common.types.railgroup.RailGroupChain
import org.webctc.common.types.tecon.TeConAction
import org.webctc.common.types.tecon.TeConLeverRuntimeState
import org.webctc.common.types.tecon.TeConLeverSide
import org.webctc.common.types.tecon.operation.ITeConOperation
import org.webctc.common.types.tecon.operation.JavaScriptOperation
import org.webctc.common.types.tecon.operation.RedStoneOperation
import org.webctc.common.types.tecon.operation.ReserveOperation
import org.webctc.common.types.tecon.shape.*
import react.*
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text
import react.dom.svg.TextAnchor
import utils.removeAtNew
import utils.setNew
import web.cssom.*
import web.html.HTMLSelectElement
import web.html.InputType
import web.html.number

external interface TeConLeverElementProps : ITeConElementProps, PreviewElementProps, IShapeElementProps<TeConLever> {
    var runtimeState: TeConLeverRuntimeState?
    var armedSide: TeConLeverSide?
    var onOperate: ((TeConLeverSide) -> Unit)?
}

private enum class SideKind(val value: String) {
    DISABLED("disabled"),
    DIRECT("direct"),
    SELECT("select"),
}

val TeConLeverElement = FC<TeConLeverElementProps> { props ->
    val lever = props.iShape
    val pos = lever.pos
    val runtime = props.runtimeState
    val armedSide = props.armedSide

    fun sideColor(side: TeConLeverSide, config: TeConLeverSideConfig): String {
        if (config is DisabledLeverSide) return "#303030"
        if (runtime?.activeSide == side) return if (runtime.cancelable) "gold" else "orange"
        if (armedSide == side) return "lightgreen"
        return "#d0d0d0"
    }

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y}) rotate(${lever.rotation})"
        stroke = "white"

        line {
            x1 = -28.0
            y1 = 0.0
            x2 = 28.0
            y2 = 0.0
            strokeWidth = 4.0
        }
        circle {
            r = 6.0
            fill = "white"
            stroke = "none"
        }

        LeverSideRect {
            side = TeConLeverSide.L
            x = -42.0
            fillColor = sideColor(TeConLeverSide.L, lever.left)
            disabled = lever.left is DisabledLeverSide
            onOperate = props.onOperate
            mode = props.mode
        }
        LeverSideRect {
            side = TeConLeverSide.R
            x = 14.0
            fillColor = sideColor(TeConLeverSide.R, lever.right)
            disabled = lever.right is DisabledLeverSide
            onOperate = props.onOperate
            mode = props.mode
        }

        text {
            y = -18.0
            textAnchor = TextAnchor.middle
            fill = "white"
            stroke = "none"
            pointerEvents = "none"
            +(lever.name.ifBlank { lever.id }.ifBlank { "Lever" })
        }
        text {
            x = -28.0
            y = 6.0
            textAnchor = TextAnchor.middle
            fill = if (lever.left is DisabledLeverSide) "#505050" else "black"
            stroke = "none"
            pointerEvents = "none"
            +"L"
        }
        text {
            x = 28.0
            y = 6.0
            textAnchor = TextAnchor.middle
            fill = if (lever.right is DisabledLeverSide) "#505050" else "black"
            stroke = "none"
            pointerEvents = "none"
            +"R"
        }
    }
}

private external interface LeverSideRectProps : Props {
    var side: TeConLeverSide
    var x: Double
    var fillColor: String
    var disabled: Boolean
    var onOperate: ((TeConLeverSide) -> Unit)?
    var mode: components.tecon.editor.EditMode?
}

private val LeverSideRect = FC<LeverSideRectProps> { props ->
    rect {
        x = props.x
        y = -14.0
        width = 28.0
        height = 28.0
        rx = 4.0
        ry = 4.0
        fill = props.fillColor
        strokeWidth = 2.0
        if (props.mode == null && !props.disabled) {
            css {
                cursor = Cursor.pointer
            }
            onClick = {
                it.stopPropagation()
                props.onOperate?.invoke(props.side)
            }
        }
    }
}

val TeConLeverProperty = FC<IShapePropertyElementProps<TeConLever>> { props ->
    val lever = props.iShape
    val handleChange = props.onChange
    val availableRoutes = props.routes.orEmpty()

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"TeCon Lever"
        }

        LeverTextField {
            label = "ID"
            value = lever.id
            onChange = { handleChange(lever.copy(id = it)) }
        }
        LeverTextField {
            label = "Name"
            value = lever.name
            onChange = { handleChange(lever.copy(name = it)) }
        }
        Box {
            sx { paddingBlock = 8.px }
            TextField {
                label = ReactNode("Rotation")
                type = InputType.number
                value = lever.rotation
                fullWidth = true
                this.onChange = { event ->
                    handleChange(
                        lever.copy(
                            rotation = event.target.unsafeCast<HTMLInputElement>().value.toIntOrNull() ?: 0
                        )
                    )
                }
            }
        }

        LeverSideEditor {
            title = "L側設定"
            sideConfig = lever.left
            routes = availableRoutes
            onChange = { handleChange(lever.copy(left = it)) }
        }

        LeverSideEditor {
            title = "R側設定"
            sideConfig = lever.right
            routes = availableRoutes
            onChange = { handleChange(lever.copy(right = it)) }
        }
    }
}

private external interface LeverTextFieldProps : Props {
    var label: String
    var value: String
    var onChange: (String) -> Unit
}

private val LeverTextField = FC<LeverTextFieldProps> { props ->
    Box {
        sx { paddingBlock = 8.px }
        TextField {
            label = ReactNode(props.label)
            value = props.value
            fullWidth = true
            this.onChange = { event ->
                props.onChange(event.target.unsafeCast<HTMLInputElement>().value)
            }
        }
    }
}

private external interface LeverSideEditorProps : Props {
    var title: String
    var sideConfig: TeConLeverSideConfig
    var routes: List<Route>
    var onChange: (TeConLeverSideConfig) -> Unit
}

private val LeverSideEditor = FC<LeverSideEditorProps> { props ->
    val kind = when (props.sideConfig) {
        is DirectLeverSide -> SideKind.DIRECT
        is SelectLeverSide -> SideKind.SELECT
        else -> SideKind.DISABLED
    }

    Box {
        sx {
            paddingBlock = 12.px
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
        }
        Typography {
            sx { fontWeight = FontWeight.bold }
            +props.title
        }
        Select {
            value = kind.value
            size = Size.small
            fullWidth = true
            onChange = { event, _ ->
                val value = event.unsafeCast<ChangeEvent<HTMLSelectElement, HTMLSelectElement>>().target.value
                props.onChange(
                    when (value) {
                        SideKind.DIRECT.value -> DirectLeverSide(TeConAction(name = "Direct"))
                        SideKind.SELECT.value -> SelectLeverSide()
                        else -> DisabledLeverSide()
                    }
                )
            }
            MenuItem {
                value = SideKind.DISABLED.value
                +"未使用"
            }
            MenuItem {
                value = SideKind.DIRECT.value
                +"進路てこ式"
            }
            MenuItem {
                value = SideKind.SELECT.value
                +"進路選別式"
            }
        }

        when (val sideConfig = props.sideConfig) {
            is DirectLeverSide -> {
                TeConActionEditor {
                    action = sideConfig.action
                    routes = props.routes
                    allowRoute = false
                    onChange = { props.onChange(sideConfig.copy(action = it.copy(routeId = null))) }
                }
            }

            is SelectLeverSide -> {
                Box {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                        alignItems = AlignItems.center
                    }
                    Typography { +"Actions" }
                    Button {
                        +"Add"
                        variant = ButtonVariant.outlined
                        onClick = {
                            props.onChange(
                                sideConfig.copy(
                                    actions = sideConfig.actions + TeConAction(
                                        routeId = props.routes.firstOrNull()?.id.orEmpty(),
                                        name = "Select"
                                    )
                                )
                            )
                        }
                    }
                }
                sideConfig.actions.forEachIndexed { index, action ->
                    Card {
                        sx { padding = 8.px }
                        TeConActionEditor {
                            key = Key("${action.routeId}-${action.name}-$index")
                            this.action = action
                            routes = props.routes
                            allowRoute = true
                            onChange = {
                                props.onChange(sideConfig.copy(actions = sideConfig.actions.setNew(index, it)))
                            }
                            onDelete = {
                                props.onChange(sideConfig.copy(actions = sideConfig.actions.removeAtNew(index)))
                            }
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}

private external interface TeConActionEditorProps : Props {
    var action: TeConAction
    var routes: List<Route>
    var allowRoute: Boolean
    var onChange: (TeConAction) -> Unit
    var onDelete: (() -> Unit)?
}

private val TeConActionEditor = FC<TeConActionEditorProps> { props ->
    val action = props.action

    Box {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }
            Typography { +"Action" }
            props.onDelete?.let { onDelete ->
                IconButton {
                    Delete {}
                    onClick = { onDelete() }
                }
            }
        }
        if (props.allowRoute) {
            Select {
                value = action.routeId.orEmpty()
                size = Size.small
                fullWidth = true
                onChange = { event, _ ->
                    val value = event.unsafeCast<ChangeEvent<HTMLSelectElement, HTMLSelectElement>>().target.value
                    props.onChange(action.copy(routeId = value))
                }
                props.routes.forEach { route ->
                    MenuItem {
                        value = route.id
                        +(route.name.ifBlank { route.id })
                    }
                }
            }
        }
        TextField {
            label = ReactNode("Action Name")
            value = action.name
            fullWidth = true
            this.onChange = { event ->
                props.onChange(action.copy(name = event.target.unsafeCast<HTMLInputElement>().value))
            }
        }
        FormControlLabel {
            control = Checkbox.create {
                checked = action.requireNoTrainToCancel
                this.onChange = { _, checked -> props.onChange(action.copy(requireNoTrainToCancel = checked)) }
            }
            label = ReactNode("在線中は復位できないようにする")
        }

        OperationListEditor {
            operations = action.operations
            onChange = { props.onChange(action.copy(operations = it)) }
        }
    }
}

private external interface OperationListEditorProps : Props {
    var operations: Set<ITeConOperation>
    var onChange: (Set<ITeConOperation>) -> Unit
}

private val OperationListEditor = FC<OperationListEditorProps> { props ->
    Box {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }
            Typography { +"Operations" }
            Button {
                +"Add"
                variant = ButtonVariant.outlined
                onClick = { props.onChange(props.operations + ReserveOperation()) }
            }
        }

        props.operations.forEachIndexed { index, operation ->
            Card {
                sx { padding = 8.px }
                OperationEditor {
                    key = Key("$index-${operation::class.simpleName}")
                    this.operation = operation
                    onChange = { props.onChange(props.operations.setNew(index, it)) }
                    onDelete = { props.onChange(props.operations.removeAtNew(index)) }
                }
            }
        }
    }
}

private external interface OperationEditorProps : Props {
    var operation: ITeConOperation
    var onChange: (ITeConOperation) -> Unit
    var onDelete: () -> Unit
}

private val OperationEditor = FC<OperationEditorProps> { props ->
    val operation = props.operation
    val operationType = when (operation) {
        is ReserveOperation -> "reserve"
        is RedStoneOperation -> "redstone"
        is JavaScriptOperation -> "javascript"
        else -> "reserve"
    }

    Box {
        sx {
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }
            Select {
                value = operationType
                size = Size.small
                onChange = { event, _ ->
                    val value = event.unsafeCast<ChangeEvent<HTMLSelectElement, HTMLSelectElement>>().target.value
                    props.onChange(
                        when (value) {
                            "redstone" -> RedStoneOperation()
                            "javascript" -> JavaScriptOperation()
                            else -> ReserveOperation()
                        }
                    )
                }
                MenuItem {
                    value = "reserve"
                    +"RailGroupManager#reserve"
                }
                MenuItem {
                    value = "redstone"
                    +"RedStone"
                }
                MenuItem {
                    value = "javascript"
                    disabled = true
                    +"JavaScript"
                }
            }
            IconButton {
                Delete {}
                onClick = { props.onDelete() }
            }
        }

        when (operation) {
            is ReserveOperation -> {
                TextField {
                    label = ReactNode("Key")
                    value = operation.key
                    fullWidth = true
                    this.onChange = { event ->
                        props.onChange(operation.copy(key = event.target.unsafeCast<HTMLInputElement>().value))
                    }
                }
                BoxRailGroupList {
                    title = "RailGroups"
                    railGroupList = operation.chain.chain
                    updateRailGroupList = {
                        props.onChange(operation.copy(chain = RailGroupChain(linkedSetOf(*it.toTypedArray()))))
                    }
                }
            }

            is RedStoneOperation -> BoxPosIntList {
                title = "RedStone Pos"
                wsPath = "/api/railgroups/ws/block"
                posList = operation.redStonePosSet
                updatePosList = { props.onChange(operation.copy(redStonePosSet = it)) }
            }

            is JavaScriptOperation -> TextField {
                label = ReactNode("Script")
                value = operation.script
                fullWidth = true
                multiline = true
                minRows = 6
                this.onChange = { event ->
                    props.onChange(operation.copy(script = event.target.unsafeCast<HTMLInputElement>().value))
                }
            }
        }
    }
}
