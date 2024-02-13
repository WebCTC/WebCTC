package components.tecon.editor

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
                PanToolOutlined {}
                value = EditMode.HAND
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
                +"線路"
                value = "rail"
            }
            ToggleButton {
                +"信号"
                value = "signal"
            }
            ToggleButton {
                +"進路テコ"
                value = "tecon"
            }
            ToggleButton {
                +"進路選別"
                value = "route"
            }
        }
    }
}