package components.tecon.editor.element

import org.webctc.common.types.tecon.shape.RectBox
import react.FC
import react.dom.svg.ReactSVG.rect
import kotlin.math.abs
import kotlin.math.min

external interface RectBoxElementProps : ITeConElementProps, PreviewElementProps, IShapeElementProps<RectBox>

val RectBoxElement = FC<RectBoxElementProps> { props ->
    val rectBox = props.iShape

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        selected = props.selected
        stroke = "white"

        val minX = min(rectBox.start.x, rectBox.end.x).toDouble()
        val minY = min(rectBox.start.y, rectBox.end.y).toDouble()
        val boxWidth = abs(rectBox.start.x - rectBox.end.x).toDouble()
        val boxHeight = abs(rectBox.start.y - rectBox.end.y).toDouble()

        rect {
            x = minX
            y = minY
            width = boxWidth
            height = boxHeight
            rx = 8.0
            ry = 8.0
            strokeWidth = 8.0
            if (props.preview == true) {
                opacity = 0.5
            }
        }
    }
}