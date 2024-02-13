package components.tecon.editor

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.svg.ReactSVG.g
import web.cssom.CustomPropertyName

external interface ITeConElementProps : PropsWithChildren {
    var mode: EditMode?
    var onSelect: (() -> Unit)?
    var onDelete: (() -> Unit)?
    var selected: Boolean?
}


external interface ITeConElementBaseProps : PropsWithChildren {
    var mode: EditMode?
    var fill: String
    var stroke: String
    var onSelect: (() -> Unit)?
    var onDelete: (() -> Unit)?
    var selected: Boolean?
}

val ITeConElementBase = FC<ITeConElementBaseProps> { props ->
    g {
        css {
            hover {
                if (props.mode == EditMode.ERASER) {
                    set(CustomPropertyName("fill"), "lightcoral")
                    set(CustomPropertyName("stroke"), "lightcoral")
                } else if (props.mode == EditMode.CURSOR) {
                    set(CustomPropertyName("fill"), "lightblue")
                    set(CustomPropertyName("stroke"), "lightblue")
                }
            }
            set(CustomPropertyName("fill"), if (props.selected == true) "lightblue" else props.fill)
            set(CustomPropertyName("stroke"), if (props.selected == true) "lightblue" else props.stroke)
        }

        when (props.mode) {
            EditMode.ERASER -> onClick = { props.onDelete?.let { it() } }
            EditMode.CURSOR -> onClick = { props.onSelect?.let { it() } }
            else -> {}
        }

        +props.children
    }
}