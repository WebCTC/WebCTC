package components.tecon.editor

import mui.icons.material.VisibilityOffOutlined
import mui.icons.material.VisibilityOutlined
import mui.material.ToggleButton
import mui.material.ToggleButtonGroup
import react.FC
import react.useState

external interface ToggleButtonGroupVisibilityProps : react.Props {
    var onChange: (Boolean) -> Unit
}

val ToggleButtonGroupVisibility = FC<ToggleButtonGroupVisibilityProps> { props ->
    var visibility by useState(true)

    ToggleButtonGroup {
        value = visibility
        exclusive = true
        onChange = { _, value ->
            if (value is Boolean) {
                visibility = value
                props.onChange(value)
            }
        }
        ToggleButton {
            VisibilityOutlined { }
            value = true
        }
        ToggleButton {
            VisibilityOffOutlined { }
            value = false
        }
    }
}