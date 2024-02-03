package components.tecon.editor

import mui.icons.material.ZoomInOutlined
import mui.icons.material.ZoomOutOutlined
import mui.material.ToggleButton
import mui.material.ToggleButtonGroup
import react.FC
import react.Props

external interface ToggleButtonGroupZoomProps : Props {
    var panzoom: dynamic
}

val ToggleButtonGroupZoom = FC<ToggleButtonGroupZoomProps> { props ->
    val panzoom = props.panzoom
    ToggleButtonGroup {
        ToggleButton {
            ZoomOutOutlined { }
            onClick = { _, _ ->
                val transform = panzoom?.getTransform()
                panzoom?.smoothZoom(transform.x, transform.y, 0.8)
            }
        }
        ToggleButton {
            ZoomInOutlined { }
            onClick = { _, _ ->
                val transform = panzoom?.getTransform()
                panzoom?.smoothZoom(transform.x, transform.y, 1.2)
            }
        }
    }
}