package components.tecon.editor

import mui.icons.material.HomeOutlined
import mui.material.ToggleButton
import react.FC
import react.Props

external interface ToggleButtonHomeProps : Props {
    var panzoom: dynamic
}

val ToggleButtonHome = FC<ToggleButtonHomeProps> { props ->
    val panzoom = props.panzoom
    ToggleButton {
        HomeOutlined { }
        onClick = { _, _ -> panzoom?.smoothMoveTo(0.0, 0.0) }
    }
}