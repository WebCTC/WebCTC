package components.tecon.editor

import org.webctc.common.types.PosInt2D
import react.FC
import react.Props
import react.dom.svg.ReactSVG.circle

external interface CircleMousePosProps : Props {
    var pos: PosInt2D
}

val CircleMousePos = FC<CircleMousePosProps> { props ->
    val pos = props.pos
    circle {
        r = 12.0
        cx = pos.x.toDouble()
        cy = pos.y.toDouble()
        fill = "yellow"
        opacity = 0.5
    }
}