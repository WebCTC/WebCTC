package components

import org.webctc.common.types.WayPoint
import react.FC
import react.Props
import react.dom.html.ReactHTML.title
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text

external interface WayPointProps : Props {
    var wayPoint: WayPoint
}

val WWayPoint = FC<WayPointProps> {
    val wayPoint = it.wayPoint
    val pos = wayPoint.pos
    g {
        rect {
            x = (pos.x - 2 - 4 * wayPoint.displayName.length).toDouble()
            y = pos.z.toDouble() - 8.0
            width = 8.0 * wayPoint.displayName.length + 4
            height = 10.0
            fill = "black"
            fillOpacity = "0.8"
            rx = 2.0
            ry = 2.0
        }
        text {
            +wayPoint.displayName
            x = pos.x.toDouble()
            y = pos.z.toDouble()
            fill = "white"
            fontSize = 8.0
            fontWeight = "bold"
            textAnchor = "middle"

        }
        title {
            +wayPoint.identifyName
        }
    }
}