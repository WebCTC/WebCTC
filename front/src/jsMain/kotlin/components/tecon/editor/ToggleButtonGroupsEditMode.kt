package components.tecon.editor

import components.icon.*
import mui.icons.material.PanToolOutlined
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
    var editMode by useState(EditMode.HAND)

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
            ToggleButton {
                mdiCursorDefaultOutline {}
                value = EditMode.CURSOR
            }
            ToggleButton {
                PanToolOutlined {}
                value = EditMode.HAND
            }
            ToggleButton {
                mdiEraser {}
                value = EditMode.ERASER
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
            ToggleButton {
                mdiFence {}
                value = EditMode.RAIL
            }
            ToggleButton {
                WciPolyRailLine {}
                value = EditMode.POLYLINE
            }
            ToggleButton {
                WciSignal {}
                value = EditMode.SIGNAL
            }
            ToggleButton {
                WciRouteLever {}
                value = EditMode.TECON
                disabled = true
            }
            ToggleButton {
                WciRouteSelection {}
                value = EditMode.ROUTE
                disabled = true
            }
        }
    }
}