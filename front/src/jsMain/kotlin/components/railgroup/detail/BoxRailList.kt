package components.railgroup.detail

import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import react.*
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px

external interface BoxRailListProps : Props {
    var railsStateInstance: StateInstance<Set<PosInt>>
    var selectedRails: Collection<PosInt>
    var setHoveringRail: StateSetter<PosInt?>
}

val BoxRailList = FC<BoxRailListProps> { props ->
    val (rails, setRails) = props.railsStateInstance
    val selectedRails = props.selectedRails
    val setHoveringRail = props.setHoveringRail

    Box {
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                paddingBottom = 8.px
            }
            +"Rails"
            Button {
                +"Add"
                variant = ButtonVariant.outlined
                onClick = {
                    setRails { it + selectedRails }
                }
            }
        }
        Paper {
            List {
                disablePadding = true
                rails.forEach { pos ->
                    ListItem {
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = { setRails { it - pos } }
                        }
                        ListItemButton {
                            ListItemText {
                                primary = ReactNode("${pos.x}, ${pos.y}, ${pos.z}")
                            }
                            onMouseEnter = { setHoveringRail(pos) }
                            onMouseLeave = { setHoveringRail(null) }
                        }
                    }
                }
            }
        }
    }
}