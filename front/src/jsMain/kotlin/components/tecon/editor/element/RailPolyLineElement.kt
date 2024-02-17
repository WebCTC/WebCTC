package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import org.webctc.common.types.tecon.shape.RailPolyLine
import react.FC
import react.Props
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.polyline

external interface RailPolyLineElementProps : RGStateElementProps, IShapeElementProps<RailPolyLine>

val RailPolyLineElement = FC<RailPolyLineElementProps> { props ->
    val railPolyLine = props.iShape

    val rgStateList = props.rgState ?: emptySet()

    val color =
        if (rgStateList.any { it.trainOnRail == true }) "red"
        else if (rgStateList.any { it.reserved == true }) "yellow"
        else if (rgStateList.any { it.locked == true }) "orange"
        else if (railPolyLine.railGroupList.isEmpty()) "gray"
        else "white"

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        stroke = color

        polyline {
            strokeWidth = 8.0
            points = railPolyLine.points.joinToString(" ") { "${it.x},${it.y}" }
            if (props.preview == true) {
                opacity = 0.5
            }
        }
    }
}

external interface RailPolyLinePropertyProps : Props {
    var railLine: RailPolyLine
    var onChange: (RailPolyLine) -> Unit
}

val RailPolyLineProperty = FC<RailPolyLinePropertyProps> { props ->
    val railLine = props.railLine
    val uuids = props.railLine.railGroupList
    val onChange = props.onChange

    Box {
        h2 { +"RailPolyLine" }
    }
    BoxRailGroupList {
        title = "Properties(RailPolyLine)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }
}

