package components

import org.webctc.common.types.trains.FormationData
import react.FC
import react.Props
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text
import kotlin.math.round


external interface FormationProps : Props {
    var formation: FormationData
}

val WFormation = FC<FormationProps> {
    val formation = it.formation

    g {
        val controlCar = formation.controlCar!!
        val pos = controlCar.pos
        val posX = pos[0]
        val posZ = pos[2]
        val doorState = controlCar.trainStateData[4] != 0.toByte()

        rect {
            x = posX - 3
            y = posZ - 3
            width = 6.0
            height = 6.0
            fill = if (doorState) "gray" else "yellow"
            stroke = "gray"
        }

        val speed = round(controlCar.speed * 72)
        val name = controlCar.name.let {
            if (it == "no_name") "" else it
        }
        val driver = controlCar.driver

        text {
            +"${speed}km/h $name $driver"
            x = posX + 5
            y = posZ + 3
            fontSize = 8.0
            fontWeight = "bold"
            fill = "white"
        }
    }
}