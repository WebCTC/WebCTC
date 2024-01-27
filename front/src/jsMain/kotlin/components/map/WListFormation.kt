package components.map

import components.railgroup.detail.ListItemPosIntProps
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Paper
import mui.system.sx
import org.webctc.common.types.PosDouble
import org.webctc.common.types.WayPoint
import org.webctc.common.types.trains.FormationData
import react.FC
import react.ReactNode
import web.cssom.Color


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
        sx {
            backgroundColor = Color("white")
        }
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

fun getLocationString(waypointList: List<WayPoint>, pos: PosDouble): String {
    val sortedWayPoints = waypointList.sortedBy { it.pos.distanceTo(pos) }

    return if (waypointList.isEmpty()) ""
    else if (waypointList.size == 1 || sortedWayPoints.first().pos.distanceTo(pos) < 100) {
        sortedWayPoints.first().calculatedDisplayName(pos)
    } else {
        waypointList
            .createUniquePairList()
            .sortedBy { (a, b) -> a.pos.distanceTo(pos) + b.pos.distanceTo(pos) }
            .first { (a, b) -> pos.isInsideSegment2D(a.pos, b.pos) }
            .let { (a, b) -> "${a.calculatedDisplayName(pos)} - ${b.calculatedDisplayName(pos)}" }
    }
}

fun <A> List<A>.createUniquePairList(): List<Pair<A, A>> {
    return this.flatMapIndexed { index, a ->
        this.subList(index + 1, this.size).map { b -> Pair(a, b) }
    }
}

fun WayPoint.calculatedDisplayName(pos: PosDouble): String {
    val wp = this
    return buildString {
        append(wp.displayName)
        if (pos.distanceTo(wp.pos) > 100) append("(${pos.distanceTo(wp.pos).toInt()}m)")
    }
}