package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import mui.material.TextField
import mui.material.Typography
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import org.webctc.common.types.tecon.shape.TrainNumber
import react.FC
import react.ReactNode
import react.dom.onChange
import react.dom.svg.DominantBaseline
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text
import react.dom.svg.TextAnchor
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.px
import web.html.InputType
import web.html.number
import kotlin.math.abs
import kotlin.math.min

external interface TrainNumberElementProps : ITeConElementProps, PreviewElementProps, IShapeElementProps<TrainNumber> {
    var trainCustomName: String?
}

val TrainNumberElement = FC<TrainNumberElementProps> { props ->
    val trainNumber = props.iShape
    val trainCustomName = props.trainCustomName
    val pos = trainNumber.pos

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y})"
        stroke = if (trainCustomName == null) "white" else "orange"
        fill = "gray"

        val minX = min(trainNumber.start.x, trainNumber.end.x).toDouble()
        val minY = min(trainNumber.start.y, trainNumber.end.y).toDouble()
        val boxWidth = abs(trainNumber.start.x - trainNumber.end.x).toDouble()
        val boxHeight = abs(trainNumber.start.y - trainNumber.end.y).toDouble()

        rect {
            x = minX
            y = minY
            width = boxWidth
            height = boxHeight
            strokeWidth = 4.0
            if (props.preview == true) {
                opacity = 0.5
            }
        }

        text {
            val centerX = minX + boxWidth / 2
            val centerY = minY + boxHeight / 2 + trainNumber.size.toDouble() / 2 + 4
            x = centerX
            y = centerY
            stroke = "none"
            fill = "orange"
            fontSize = trainNumber.size.toDouble()
            textAnchor = when (trainNumber.anchor) {
                "start" -> TextAnchor.start
                "middle" -> TextAnchor.middle
                "end" -> TextAnchor.end
                else -> null
            }
            dominantBaseline = DominantBaseline.textAfterEdge
            if (props.selected == true) {
                +"1234E"
            } else {
                +trainCustomName
            }
        }
    }
}

val TrainNumberProperty = FC<IShapePropertyElementProps<TrainNumber>> { props ->
    val railLine = props.iShape
    val uuids = props.iShape.railGroupList
    val onChange = props.onChange

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"TrainName"
        }
    }

    BoxRailGroupList {
        title = "Properties(TrainName)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }

    Box {
        sx {
            paddingBlock = 8.px
        }
        TextField {
            label = ReactNode("Font Size")
            type = InputType.number
            value = railLine.size
            this.onChange = { event ->
                onChange(railLine.copy(size = event.target.unsafeCast<HTMLInputElement>().value.toIntOrNull() ?: 0))
            }
            fullWidth = true
        }
    }
}
