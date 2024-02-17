package components.map

import components.railgroup.detail.ListItemPosIntProps
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Paper
import org.webctc.common.types.PosDouble
import org.webctc.common.types.trains.FormationData
import org.webctc.common.types.waypoint.WayPoint
import react.FC
import react.ReactNode
import utils.createUniquePairList


external interface ListItemFormationProps : ListItemPosIntProps {
    var formation: FormationData
    var waypointList: List<WayPoint>
    var onClick: (FormationData) -> Unit
    var selected: Boolean
}

val ListItemFormation = FC<ListItemFormationProps> {
    val formation = it.formation
    val waypointList = it.waypointList
    val onClick = it.onClick
    val selected = it.selected

    Paper {
        ListItemButton {
            ListItemText {
                val controlCarName = formation.controlCar?.name
                val speed = "${(formation.speed * 72).toInt()}km/h"
                val driver =
                    if (formation.driver.isNotEmpty()) "(${formation.driver})" else ""

                val pos = formation.controlCar?.pos

                val location = pos?.let { getLocationString(waypointList, it) } ?: ""

                val cars = formation.entities

                primary = ReactNode(buildString {
                    controlCarName.takeIf { !it.isNullOrEmpty() }?.let {
                        append(it)
                        append(" ")
                    }

                    append(speed)
                    append(" ")

                    append(driver)
                })
                secondary = ReactNode(buildString {
                    location.takeIf { it.isNotEmpty() }?.let {
                        append(it)
                        append(" ")
                    }

                    append(cars.size)
                    append("cars")
                })
            }
            this.selected = selected
            this.onClick = { onClick(formation) }
        }
    }
}

fun getLocationString(list: List<WayPoint>, pos: PosDouble): String {
    return if (list.isEmpty()) ""
    else if (list.size == 1) {
        list.first().calculatedDisplayName(pos)
    } else if (list.any { it.range.contains(pos) }) {
        list.first { it.range.contains(pos) }.displayName
    } else {
        list.createUniquePairList()
            .filter { (a, b) -> pos.isInsideSegment2D(a.pos, b.pos) }
            .filter { (a, b) -> pos.distanceToSegment(a.pos, b.pos) < a.pos.distanceTo(b.pos) }
            .minByOrNull { (a, b) ->
                a.pos.distanceTo(pos) + b.pos.distanceTo(pos) + pos.distanceToSegment(a.pos, b.pos)
            }
            ?.let { (a, b) -> "${a.calculatedDisplayName(pos)} - ${b.calculatedDisplayName(pos)}" }
            ?: ""
    }
}

fun WayPoint.calculatedDisplayName(pos: PosDouble): String {
    val wp = this
    return buildString {
        append(wp.displayName)
        append("(${pos.distanceTo(wp.pos).toInt()}m)")
    }
}
