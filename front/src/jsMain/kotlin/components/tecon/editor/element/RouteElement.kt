package components.tecon.editor.element

import emotion.react.css
import mui.material.Box
import mui.material.TextField
import mui.material.Typography
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import org.webctc.common.types.tecon.shape.Route
import react.FC
import react.ReactNode
import react.dom.onChange
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.text
import react.dom.svg.TextAnchor
import web.cssom.Cursor
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.px

external interface RouteElementProps : ITeConElementProps, PreviewElementProps, IShapeElementProps<Route> {
    var enabled: Boolean?
    var active: Boolean?
    var onTrigger: (() -> Unit)?
}

val RouteElement = FC<RouteElementProps> { props ->
    val route = props.iShape
    val pos = route.pos
    val enabled = props.enabled == true
    val active = props.active == true
    val fillColor = when {
        active -> "gold"
        enabled -> "lightgreen"
        props.mode == null -> "#404040"
        else -> "#707070"
    }

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y})"
        fill = fillColor
        stroke = "white"
        blink = enabled

        circle {
            r = 18.0
            strokeWidth = 4.0
            if (props.mode == null && enabled && props.onTrigger != null) {
                css {
                    cursor = Cursor.pointer
                }
            }
            if (props.mode == null && props.onTrigger != null) {
                onClick = {
                    it.stopPropagation()
                    props.onTrigger?.invoke()
                }
            }
            if (props.preview == true) {
                opacity = 0.5
            }
        }
        text {
            textAnchor = TextAnchor.middle
            y = 5.0
            fill = "black"
            stroke = "none"
            pointerEvents = "none"
            +(route.name.ifBlank { route.id }.ifBlank { "Route" })
        }
    }
}

val RouteProperty = FC<IShapePropertyElementProps<Route>> { props ->
    val route = props.iShape
    val handleChange = props.onChange

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"Route"
        }
        Box {
            sx { paddingBlock = 8.px }
            TextField {
                label = ReactNode("ID")
                value = route.id
                fullWidth = true
                this.onChange = { event ->
                    handleChange(route.copy(id = event.target.unsafeCast<HTMLInputElement>().value))
                }
            }
        }
        Box {
            sx { paddingBlock = 8.px }
            TextField {
                label = ReactNode("Name")
                value = route.name
                fullWidth = true
                this.onChange = { event ->
                    handleChange(route.copy(name = event.target.unsafeCast<HTMLInputElement>().value))
                }
            }
        }
    }
}
