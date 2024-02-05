package components.railgroup.detail

import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.PosIntWithKey
import react.*
import react.dom.events.ChangeEvent
import react.dom.onChange
import utils.removeAtNew
import utils.setNew
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px
import web.html.HTMLInputElement

external interface PosIntWithKeyListProps : Props {
    var title: String
    var posList: Set<PosIntWithKey>
    var updatePosList: (Set<PosIntWithKey>) -> Unit
    var wsPath: String
}

val BoxPosIntWithKeyList = FC<PosIntWithKeyListProps> { props ->
    val title = props.title
    val list = props.posList
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
                    onClick = { (list + PosIntWithKey.ZERO).also(setter) }
                }
            }
        }
        Paper {
            List {
                disablePadding = true
                list.forEachIndexed { index, pos ->
                    ListItem {
                        sx { paddingRight = 72.px }
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = {
                                list.removeAtNew(index).also(setter)
                            }
                        }

                        ListItemPosIntWithKey {
                            this.pos = pos
                            this.onChange = {
                                list.setNew(index, it).also(setter)
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
        this.onSave = { it ->
            open = null
            setter(list + it.map(::PosIntWithKey))
        }
        this.key = open?.toString()
    }
}

external interface ListItemPosIntWithKeyProps : Props {
    var pos: PosIntWithKey
    var onChange: ((PosIntWithKey) -> Unit)
}

val ListItemPosIntWithKey = FC<ListItemPosIntWithKeyProps> {
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
                this.pos = pos.toPosInt()
                this.prop = it
                this.onChange = { new -> PosIntWithKey(new).also(onChange) }
            }
        }
        TextField {
            size = Size.small
            label = ReactNode("Key")
            value = pos.key
            this.onChange = { formEvent ->
                val event = formEvent.unsafeCast<ChangeEvent<HTMLInputElement>>()
                val new = event.target.value
                PosIntWithKey(pos.x, pos.y, pos.z, new).also(onChange)
            }
        }
    }
}