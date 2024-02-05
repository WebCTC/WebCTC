package components.railgroup.detail

import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import react.FC
import react.Props
import react.ReactNode
import react.create
import utils.removeAtNew
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px

external interface BoxRailListProps : Props {
    var rails: Set<PosInt>
    var updateRails: (Set<PosInt>) -> Unit
    var selectedRails: Collection<PosInt>
    var hoveringRail: PosInt?
    var setHoveringRail: (PosInt?) -> Unit
}

val BoxRailList = FC<BoxRailListProps> { props ->
    val rails = props.rails
    val updateRails = props.updateRails
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
                onClick = { (rails + selectedRails).also(updateRails) }
            }
        }
        Paper {
            List {
                disablePadding = true
                rails.forEachIndexed { index, pos ->
                    ListItem {
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = {
                                rails.removeAtNew(index).also(updateRails)
                            }
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