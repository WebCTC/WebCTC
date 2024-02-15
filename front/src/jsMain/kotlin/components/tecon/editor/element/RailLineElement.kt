package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import org.webctc.common.types.railgroup.RailGroupState
import org.webctc.common.types.tecon.shape.RailLine
import react.FC
import react.Props
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.path
import kotlin.math.abs

external interface RailElementProps : ITeConElementProps {
    var rgState: Set<RailGroupState>?
}

external interface LineElementProps : RailElementProps {
    var rail: RailLine
}

val RailLineElement = FC<LineElementProps> { props ->
    val railLine = props.rail
    val startPos = railLine.start
    val endPos = railLine.end

    val rgStateList = props.rgState ?: emptySet()

    val color =
        if (rgStateList.any { it.trainOnRail == true }) "red"
        else if (rgStateList.any { it.reserved == true }) "yellow"
        else if (rgStateList.any { it.locked == true }) "orange"
        else if (railLine.railGroupList.isEmpty()) "gray"
        else "white"

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        fill = color
        selected = props.selected
        path {
            val startX = startPos.x
            val startY = startPos.y
            val endX = endPos.x
            val endY = endPos.y
            val horizontal = abs((endY - startY).toDouble() / (endX - startX).toDouble()) > 1
            d = if (horizontal) {
                "M ${startX - 4} $startY L ${endX - 4} $endY L ${endX + 4} $endY L ${startX + 4} $startY Z"
            } else {
                "M $startX ${startY - 4} L $endX ${endY - 4} L $endX ${endY + 4} L $startX ${startY + 4} Z"
            }
        }
    }
}

external interface RailLinePropertyProps : Props {
    var railLine: RailLine
    var onChange: (RailLine) -> Unit
}

val RailLineProperty = FC<RailLinePropertyProps> { props ->
    val railLine = props.railLine
    val uuids = props.railLine.railGroupList
    val onChange = props.onChange

    Box {
        h2 { +"RailLine" }
    }
    BoxRailGroupList {
        title = "Properties(RailLine)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }
}
