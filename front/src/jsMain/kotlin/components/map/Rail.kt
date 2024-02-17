package components.map

import emotion.react.css
import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.rail.RailMapSwitchData
import react.FC
import react.Props
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.line
import web.cssom.Cursor
import web.cssom.CustomPropertyName

external interface LargeRailProps : Props {
    var largeRailData: LargeRailData
}

external interface LargeRailClickableProps : LargeRailProps {
    var onClick: ((LargeRailData) -> Unit)?
    var color: String?
}

val WRail = FC<LargeRailProps> {
    val rail = it.largeRailData

    g {
        val isTrainOnRail = rail.isTrainOnRail
        if (isTrainOnRail) {
            stroke = "red"
        }
        id = "rail,${rail.pos}"
        rail.railMaps.forEach {
            val isNotActive = it is RailMapSwitchData && it.isNotActive
            line {
                x1 = it.startRP.posX
                y1 = it.startRP.posZ
                x2 = it.endRP.posX
                y2 = it.endRP.posZ

                if (isNotActive) stroke = "gray"
            }
        }
    }
}

val WRailHover = FC<LargeRailClickableProps> {
    val rail = it.largeRailData
    val onClick = it.onClick

    g {
        stroke = it.color
        id = "rail,${rail.pos}"
        css {
            cursor = Cursor.pointer
            hover {
                set(CustomPropertyName("stroke"), "lightblue")
            }
        }
        onClick?.let { this.onClick = { it(rail) } }
        rail.railMaps.forEach {
            line {
                x1 = it.startRP.posX
                y1 = it.startRP.posZ
                x2 = it.endRP.posX
                y2 = it.endRP.posZ

            }
        }
    }
}