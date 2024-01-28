package components.waypoint

import client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import js.objects.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.ExpandMore
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosDouble
import org.webctc.common.types.waypoint.WayPoint
import org.webctc.common.types.waypoint.range.CircleRange
import org.webctc.common.types.waypoint.range.RectangleRange
import react.*
import react.dom.aria.ariaReadOnly
import react.dom.events.FocusEvent
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px
import web.html.HTMLInputElement
import web.html.InputType
import kotlin.reflect.KProperty1


external interface AccordionWayPointProps : Props {
    var waypoint: WayPoint
    var updateWayPoint: (WayPoint) -> Unit
    var deleteWayPoint: (String) -> Unit
}

val AccordionWayPoint = FC<AccordionWayPointProps> {
    val waypoint = it.waypoint
    val updateWayPoint = it.updateWayPoint

    var name by useState(waypoint.displayName)
    var pos by useState(waypoint.pos)
    var range by useState(waypoint.range)

    var sending by useState(false)

    val sendWayPoint = {
        sending = true

        val changedWayPoint = WayPoint(waypoint.identifyName, name, pos, range)

        MainScope().launch {
            val response: WayPoint = client.post("/api/waypoints/update") {
                parameter("id", waypoint.identifyName)
                contentType(ContentType.Application.Json)
                setBody(changedWayPoint)
            }.body()
            waypoint.updateBy(response)
            updateWayPoint(waypoint)
            sending = false
        }
    }

    val deleteWayPoint = { id: String ->
        MainScope().launch {
            client.post("/api/waypoints/delete") {
                parameter("id", id)
            }
            it.deleteWayPoint(id)
        }
    }


    Accordion {
        AccordionSummary {
            expandIcon = ExpandMore.create {}
            +"${waypoint.displayName} (${waypoint.identifyName})"
        }
        AccordionDetails {
            Box {
                +"Display Name"
                Box {
                    OutlinedInput {
                        size = Size.small
                        value = name
                        onChange = { name = it.target.unsafeCast<HTMLInputElement>().value }
                    }
                }
            }

            Box {
                +"ID"
                Box {
                    OutlinedInput {
                        size = Size.small
                        value = waypoint.identifyName
                        inputProps = jso { ariaReadOnly = true }
                    }
                }
            }

            Box {
                +"Pos"
                Box {
                    sx {
                        display = Display.flex
                        padding = 6.px
                        gap = 8.px
                    }
                    arrayOf(PosDouble::x, PosDouble::y, PosDouble::z).forEach {
                        TextFieldPosDouble {
                            this.pos = pos
                            this.prop = it
                            this.onChange = { pos = it }
                        }
                    }
                }
            }

            Box {
                +"Station Range"
                Box {
                    when (range) {
                        is CircleRange -> {
                            val circleRange = (range as CircleRange).copy()
                            Box {
                                +"CenterPos"
                            }
                            Box {
                                sx {
                                    display = Display.flex
                                    padding = 6.px
                                    gap = 8.px
                                }
                                arrayOf(PosDouble::x, PosDouble::y, PosDouble::z).forEach {
                                    TextFieldPosDouble {
                                        this.pos = circleRange.center
                                        this.prop = it
                                        this.onChange = {
                                            range = circleRange.copy(center = it)
                                        }
                                    }
                                }
                            }
                            Box {
                                +"Radius"
                            }
                            Box {
                                OutlinedInput {
                                    size = Size.small
                                    defaultValue = circleRange.radius
                                    type = "number"
                                    onBlur = { focusEvent ->
                                        val event = focusEvent.unsafeCast<FocusEvent<HTMLInputElement>>()
                                        val target = event.target
                                        val axisValue = target.value.toDoubleOrNull() ?: 0.0
                                        if (axisValue != circleRange.radius) {
                                            circleRange.radius = axisValue
                                            range = circleRange
                                            target.value = axisValue.toString()
                                        }
                                    }
                                }
                            }
                        }

                        is RectangleRange -> {
                            val rectangleRange = (range as RectangleRange).copy()

                            Box {
                                +"StartPos"
                            }
                            Box {
                                sx {
                                    display = Display.flex
                                    padding = 6.px
                                    gap = 8.px
                                }
                                arrayOf(PosDouble::x, PosDouble::y, PosDouble::z).forEach {
                                    TextFieldPosDouble {
                                        this.pos = rectangleRange.start
                                        this.prop = it
                                        this.onChange = {
                                            range = rectangleRange.copy(start = it)
                                        }
                                    }
                                }
                            }
                            Box {
                                +"EndPos"
                            }
                            Box {
                                sx {
                                    display = Display.flex
                                    padding = 6.px
                                    gap = 8.px
                                }
                                arrayOf(PosDouble::x, PosDouble::y, PosDouble::z).forEach {
                                    TextFieldPosDouble {
                                        this.pos = rectangleRange.end
                                        this.prop = it
                                        this.onChange = {
                                            range = rectangleRange.copy(end = it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                Button {
                    +"Save"
                    onClick = { sendWayPoint() }
                    disabled = waypoint.displayName == name && waypoint.pos == pos && waypoint.range == range || sending
                    variant = ButtonVariant.contained
                }

                Button {
                    +"Delete"
                    onClick = { deleteWayPoint(waypoint.identifyName) }
                    color = ButtonColor.error
                    variant = ButtonVariant.outlined
                }
            }
        }
    }
}

external interface TextFieldPosDoubleProps : Props {
    var pos: PosDouble
    var prop: KProperty1<PosDouble, Double>
    var onChange: (PosDouble) -> Unit
}

private val TextFieldPosDouble = FC<TextFieldPosDoubleProps> {
    val pos = it.pos
    val prop = it.prop
    val onChange = it.onChange
    TextField {
        size = Size.small
        defaultValue = prop.get(pos)
        type = InputType.number
        label = ReactNode(prop.name)
        onBlur = { focusEvent ->
            val event = focusEvent.unsafeCast<FocusEvent<HTMLInputElement>>()
            val target = event.target
            val axisValue = target.value.toDoubleOrNull() ?: 0.0
            if (axisValue != prop.get(pos)) {
                pos.copy(
                    x = if (prop.name == "x") axisValue else pos.x,
                    y = if (prop.name == "y") axisValue else pos.y,
                    z = if (prop.name == "z") axisValue else pos.z
                ).also { onChange(it) }
                target.value = axisValue.toString()
            }
        }
    }
}