package components.tecon.editor

import components.icon.*
import mui.icons.material.Crop169
import mui.icons.material.PanToolOutlined
import mui.icons.material.PinOutlined
import mui.icons.material.TextFields
import mui.material.Box
import mui.material.ToggleButton
import mui.material.ToggleButtonGroup
import mui.system.sx
import react.FC
import react.Props
import react.useState
import web.cssom.Display
import web.cssom.px

external interface ToggleButtonGroupEditModeProps : Props {
    var onChange: (EditMode) -> Unit
}

val ToggleButtonGroupEditMode = FC<ToggleButtonGroupEditModeProps> {
    var editMode by useState<EditMode>(EditMode.HAND)

    Box {
        sx {
            display = Display.flex
            gap = 8.px
        }
        ToggleButtonGroup {
            exclusive = true
            value = editMode
            onChange = { _, value ->
                if (value is EditMode) {
                    editMode = value
                    it.onChange(value)
                }
            }
            EditModeToggleButton {
                mode = EditMode.CURSOR
                icon = mdiCursorDefaultOutline
            }
            EditModeToggleButton {
                mode = EditMode.HAND
                icon = PanToolOutlined
            }
            EditModeToggleButton {
                mode = EditMode.ERASER
                icon = mdiEraser
            }
        }
        ToggleButtonGroup {
            exclusive = true
            value = editMode
            onChange = { _, value ->
                if (value is EditMode) {
                    editMode = value
                    it.onChange(value)
                }
            }
            EditModeToggleButton {
                mode = EditMode.RAIL
                icon = mdiFence
            }
            EditModeToggleButton {
                mode = EditMode.POLYLINE
                icon = WciPolyRailLine
            }
            EditModeToggleButton {
                mode = EditMode.SIGNAL
                icon = WciSignal
            }
            EditModeToggleButton {
                mode = EditMode.TRAIN_NUMBER
                icon = PinOutlined
            }
            EditModeToggleButton {
                mode = EditMode.TECON
                icon = WciRouteLever
                disabled = true
            }
            EditModeToggleButton {
                mode = EditMode.ROUTE
                icon = WciRouteSelection
                disabled = true
            }
        }

        ToggleButtonGroup {
            exclusive = true
            value = editMode
            onChange = { _, value ->
                if (value is EditMode) {
                    editMode = value
                    it.onChange(value)
                }
            }
            EditModeToggleButton {
                mode = EditMode.RECT
                icon = Crop169
            }
            EditModeToggleButton {
                mode = EditMode.FREETEXT
                icon = TextFields
            }
        }
    }
}

external interface EditModeToggleButtonProps : Props {
    var mode: EditMode
    var icon: FC<*>
    var disabled: Boolean?
}

val EditModeToggleButton = FC<EditModeToggleButtonProps> {
    val mode = it.mode
    val icon = it.icon
    val disabled = it.disabled ?: false
    ToggleButton {
        this.disabled = disabled
        title = mode.name
        icon()
        value = mode
    }
}