package components.tecon.editor.element

import components.tecon.editor.EditMode
import emotion.css.keyframes
import emotion.react.css
import org.webctc.common.types.tecon.shape.IShape
import org.webctc.common.types.tecon.shape.Route
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.svg.ReactSVG.g
import web.cssom.*

external interface ITeConElementProps : PropsWithChildren {
    var mode: EditMode?
    var onSelect: (() -> Unit)?
    var onDelete: (() -> Unit)?
    var selected: Boolean?
}

external interface PreviewElementProps : Props {
    var preview: Boolean?
}

external interface IShapeElementProps<out T : IShape> : Props {
    var iShape: @UnsafeVariance T
}

external interface IShapePropertyElementProps<out T : IShape> : Props {
    var iShape: @UnsafeVariance T
    var onChange: (@UnsafeVariance T) -> Unit
    var routes: List<Route>?
}

external interface ITeConElementBaseProps : PropsWithChildren {
    var mode: EditMode?
    var fill: String?
    var stroke: String?
    var onSelect: (() -> Unit)?
    var onDelete: (() -> Unit)?
    var selected: Boolean?
    var transform: String?
    var blink: Boolean?
}

val ITeConElementBase = FC<ITeConElementBaseProps> { props ->
    var fillColor = if (props.fill == null) "none" else if (props.selected == true) "lightblue" else props.fill
    var strokeColor = if (props.stroke == null) "none" else if (props.selected == true) "skyblue" else props.stroke
    g {
        css {
            userSelect = None.none
            hover {
                if (props.mode == EditMode.ERASER) {
                    props.fill?.let { set(CustomPropertyName("fill"), "lightcoral") }
                    props.stroke?.let { set(CustomPropertyName("stroke"), "lightcoral") }
                } else if (props.mode == EditMode.CURSOR) {
                    props.fill?.let { set(CustomPropertyName("fill"), "lightblue") }
                    props.stroke?.let { set(CustomPropertyName("stroke"), "skyblue") }
                }
            }
            set(CustomPropertyName("fill"), fillColor)
            set(CustomPropertyName("stroke"), strokeColor)
            val keyFrameName = keyframes {
                from {
                    set(CustomPropertyName("fill"), fillColor)
                }
                50.pct {
                    set(CustomPropertyName("fill"), "#202020")
                }
                to {
                    set(CustomPropertyName("fill"), fillColor)
                }
            }
            if (props.blink == true) {
                animationName = keyFrameName
                animationDuration = 1.s
                animationIterationCount = AnimationIterationCount.infinite
                animationTimingFunction = AnimationTimingFunction.stepStart
            }
        }
        transform = props.transform

        when (props.mode) {
            EditMode.ERASER -> onClick = { props.onDelete?.let { it() } }
            EditMode.CURSOR -> onClick = { props.onSelect?.let { it() } }
            else -> {}
        }

        +props.children
    }
}
