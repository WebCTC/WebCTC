package components.map

import org.webctc.common.types.WayPoint
import react.FC
import react.Props
import react.dom.html.ReactHTML.title
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text

external interface WayPointProps : Props {
    var wayPoint: WayPoint
    var scale: Double?
}

val WWayPoint = FC<WayPointProps> {
    val wayPoint = it.wayPoint
    val pos = wayPoint.pos
    g {
        transform = "translate(${pos.x} ${pos.z}) scale(${2 / (it.scale ?: 1.0)})"
        rect {
            x = (-2 - 4 * wayPoint.displayName.length).toDouble()
            y = -8.0
            width = 8.0 * wayPoint.displayName.length + 4
            height = 10.0
            fill = "black"
            fillOpacity = "0.8"
            rx = 2.0
            ry = 2.0
        }
        text {
            +wayPoint.displayName
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