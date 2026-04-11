package components.railgroup

import mui.icons.material.ArrowDropDown
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.useState
import web.cssom.None
import web.dom.Element

external interface CreateSplitButtonProps : Props {
    var onCreateRailGroup: () -> Unit
    var onCreateFolder: () -> Unit
}

val CreateSplitButton = FC<CreateSplitButtonProps> { props ->
    var anchorEl by useState<Element?>(null)
    var selectedIndex by useState(0)
    val open = anchorEl != null

    val options: List<Pair<String, () -> Unit>> = listOf(
        "RailGroup を作成" to props.onCreateRailGroup,
        "フォルダを作成" to props.onCreateFolder,
    )

    ButtonGroup {
        variant = ButtonGroupVariant.contained
        Button {
            sx {
                textTransform = None.none
            }
            +options[selectedIndex].first
            onClick = { options[selectedIndex].second() }
        }
        Button {
            size = Size.small
            onClick = { e -> anchorEl = e.currentTarget }
            ArrowDropDown {}
        }
    }

    Menu {
        this.open = open
        this.anchorEl = { anchorEl ?: it }
        onClose = { anchorEl = null }
        options.forEachIndexed { index, (label, _) ->
            MenuItem {
                selected = index == selectedIndex
                onClick = {
                    selectedIndex = index
                    anchorEl = null
                }
                +label
            }
        }
    }
}
