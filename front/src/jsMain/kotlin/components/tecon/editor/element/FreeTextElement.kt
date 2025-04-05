package components.tecon.editor.element

import mui.material.Box
import mui.material.TextField
import mui.material.Typography
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import org.webctc.common.types.tecon.shape.FreeText
import react.FC
import react.dom.onChange
import react.dom.svg.ReactSVG.text
import web.cssom.FontSize
import web.cssom.FontWeight

external interface FreeTextElementProps : ITeConElementProps, IShapeElementProps<FreeText>

var FreeTextElement = FC<FreeTextElementProps> { props ->
    val textShape = props.iShape
    val pos = textShape.pos

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y})"
        stroke = "white"

        text {
            fill = textShape.color
            fontSize = textShape.size.toDouble()
            textAnchor = textShape.anchor
            +textShape.text
        }
    }
}

val FreeTextProperty = FC<IShapePropertyElementProps<FreeText>> { props ->
    val textShape = props.iShape
    val onChange = props.onChange

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"Text"
        }

        TextField {
            value = textShape.text
            this.onChange = { event ->
                onChange(textShape.copy(text = event.target.unsafeCast<HTMLInputElement>().value))
            }
            fullWidth = true
        }
    }
}