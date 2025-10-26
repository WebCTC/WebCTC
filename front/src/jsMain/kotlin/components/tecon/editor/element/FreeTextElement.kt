package components.tecon.editor.element

import mui.icons.material.FormatAlignCenter
import mui.icons.material.FormatAlignLeft
import mui.icons.material.FormatAlignRight
import mui.material.*
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import org.webctc.common.types.tecon.shape.FreeText
import react.FC
import react.ReactNode
import react.dom.onChange
import react.dom.svg.ReactSVG.text
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.px
import web.html.InputType

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

        Box {
            sx {
                paddingBlock = 8.px
            }
            TextField {
                label = ReactNode("Text")
                value = textShape.text
                this.onChange = { event ->
                    onChange(textShape.copy(text = event.target.unsafeCast<HTMLInputElement>().value))
                }
                fullWidth = true
            }
        }

        Box {
            sx {
                paddingBlock = 8.px
            }
            ToggleButtonGroup {
                value = textShape.anchor
                exclusive = true
                this.onChange = { _, newValue ->
                    if (newValue is String) {
                        onChange(textShape.copy(anchor = newValue))
                    }
                }
                ToggleButton {
                    title = "Left Align"
                    value = "start"
                    FormatAlignLeft {}
                }
                ToggleButton {
                    title = "Center Align"
                    value = "middle"
                    FormatAlignCenter {}
                }
                ToggleButton {
                    title = "Right Align"
                    value = "end"
                    FormatAlignRight {}
                }
            }
        }

        Box {
            sx {
                paddingBlock = 8.px
            }
            TextField {
                label = ReactNode("Font Size")
                type = InputType.number
                value = textShape.size
                this.onChange = { event ->
                    onChange(
                        textShape.copy(
                            size = event.target.unsafeCast<HTMLInputElement>().value.toIntOrNull() ?: 0
                        )
                    )
                }
                fullWidth = true
            }
        }
    }
}