package components.railgroup.detail

import js.objects.jso
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import react.*
import react.dom.aria.ariaReadOnly
import react.dom.events.FocusEvent
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px
import web.html.HTMLInputElement
import web.html.InputType
import kotlin.reflect.KProperty1

external interface PosIntListProps : Props {
    var title: String
    var wsPath: String
    var posList: Set<PosInt>
    var updatePosList: (Set<PosInt>) -> Unit
}

val BoxPosIntList = FC<PosIntListProps> { props ->
    val title = props.title
    val posList = props.posList
    val setter = props.updatePosList
    val wsPath = props.wsPath

    var open by useState<UUID?> { null }

    Box {
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                paddingBottom = 8.px
            }
            +title
            Box {
                sx {
                    display = Display.flex
                    gap = 8.px
                }
                Button {
                    +"Receive"
                    variant = ButtonVariant.outlined
                    onClick = { open = UUID.generateUUID() }
                }
                Button {
                    +"Add"
                    variant = ButtonVariant.outlined
                    onClick = { setter(posList + PosInt.ZERO) }
                }
            }
        }
        Paper {
            List {
                disablePadding = true
                posList.forEachIndexed { index, pos ->
                    ListItem {
                        sx { paddingRight = 72.px }
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = { setter(posList - pos) }
                        }

                        ListItemPosInt {
                            this.pos = pos
                            this.onChange = { new ->
                                setter(
                                    posList.toMutableList()
                                        .apply { this[index] = new }
                                        .toMutableSet()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DialogReceivePos {
        this.title = title
        this.wsPath = wsPath
        this.uuid = open
        this.onSave = { list ->
            open = null
            setter(posList + list)
        }
        this.key = open?.toString()
    }
}

external interface ListItemPosIntProps : Props {
    var pos: PosInt
    var onChange: ((PosInt) -> Unit)?
}

val ListItemPosInt = FC<ListItemPosIntProps> {
    val pos = it.pos
    val onChange = it.onChange
    Box {
        sx {
            display = Display.flex
            padding = 6.px
            gap = 8.px
        }
        arrayOf(PosInt::x, PosInt::y, PosInt::z).forEach {
            TextFieldPosInt {
                this.pos = pos
                this.prop = it
                this.onChange = onChange
            }
        }
    }
}

external interface TextFieldPosIntProps : Props {
    var pos: PosInt
    var prop: KProperty1<PosInt, Int>
    var onChange: ((PosInt) -> Unit)?
}

val TextFieldPosInt = FC<TextFieldPosIntProps> {
    val pos = it.pos
    val prop = it.prop
    val onChange = it.onChange
    TextField {
        size = Size.small
        defaultValue = prop.get(pos)
        type = InputType.number
        label = ReactNode(prop.name)
        if (onChange == null) {
            inputProps = jso { ariaReadOnly = true }
        } else {
            onBlur = { focusEvent ->
                val event = focusEvent.unsafeCast<FocusEvent<HTMLInputElement>>()
                val target = event.target
                val axisValue = target.value.toIntOrNull() ?: 0
                if (axisValue != prop.get(pos)) {
                    pos.copy(
                        x = if (prop.name == "x") axisValue else pos.x,
                        y = if (prop.name == "y") axisValue else pos.y,
                        z = if (prop.name == "z") axisValue else pos.z
                    ).also(onChange)
                    target.value = axisValue.toString()
                }
            }
        }
    }
}