package components

import org.webctc.common.types.rail.LargeRailData
import org.webctc.common.types.rail.RailMapSwitchData
import react.FC
import react.Props
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.line

external interface LargeRailProps : Props {
    var largeRailData: LargeRailData
}

val LargeRail = FC<LargeRailProps> { props ->
    val rail = props.largeRailData

    g {
        val isTrainOnRail = rail.isTrainOnRail
        stroke = if (isTrainOnRail) "red" else "white"
        id = "rail,${rail.pos.joinToString(",")}"
        rail.railMaps.forEach {
            val isNotActive = it is RailMapSwitchData && it.isNotActive
            line {
                x1 = it.startRP.posX
                y1 = it.startRP.posZ
                x2 = it.endRP.posX
                y2 = it.endRP.posZ

                if (isNotActive) {
                    stroke = "gray"
                }
            }
        }
    }
}