package components.tecon.editor

import components.railgroup.detail.TextFieldPosIntXYZ
import mui.material.*
import mui.system.responsive
import mui.system.sx
import org.webctc.common.types.signal.SignalState
import org.webctc.common.types.tecon.shape.Signal
import pages.xs
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.line
import utils.useDataWithWebsocket
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.px
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

external interface SignalElementProps : ITeConElementProps {
    var signal: Signal
}

val SignalElement = FC<SignalElementProps> { props ->
    val signalPos = props.signal.signalPos
    val pos = props.signal.pos
    val rotation = props.signal.rotation
    val x = pos.x
    val y = pos.y

    val signalLevel = if (props.mode == null) useDataWithWebsocket<SignalState>(
        "/api/signals/${signalPos.toLong()}/state",
        "/api/signals/${signalPos.toLong()}/state/ws"
    ) { a, b -> a == b }.let {
        val state by it
        (state?.level ?: 0) > 1
    } else false

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        fill = (if (signalLevel) "LawnGreen" else "#202020")
        stroke = "white"
        selected = props.selected
        rotation?.let {
            val cos = cos((-rotation - 90) * (PI / 180))
            val sin = sin((-rotation - 90) * (PI / 180))
            line {
                x1 = x.toDouble() + 12 * cos
                y1 = y.toDouble() - 12 * sin
                x2 = x.toDouble() + 32 * cos
                y2 = y.toDouble() - 32 * sin
                strokeWidth = 4.0
            }
            line {
                x1 = x.toDouble() + 32 * cos + 12 * sin
                y1 = y.toDouble() + 12 * cos - 32 * sin
                x2 = x.toDouble() + 32 * cos - 12 * sin
                y2 = y.toDouble() - 12 * cos - 32 * sin
                strokeWidth = 4.0
            }
        }
        circle {
            cx = x.toDouble()
            cy = y.toDouble()
            r = 12.0
            strokeWidth = 4.0
        }
    }
}

external interface SignalPropertyProps : Props {
    var signal: Signal
    var onChange: (Signal) -> Unit
}

val SignalProperty = FC<SignalPropertyProps> { props ->
    val signal = props.signal
    val onChange = props.onChange
    val pos = signal.signalPos
    Box {
        h2 {
            +"Signal"
        }
        Box {
            sx {
                display = Display.flex
                padding = 6.px
                gap = 8.px
            }
            TextFieldPosIntXYZ {
                this.pos = pos
                this.onChange = { onChange(signal.copy(signalPos = it)) }
            }
        }
        Box {
            Box {
                FormControlLabel {
                    label = ReactNode("Show Baseline")
                    control = Checkbox.create {
                        checked = signal.rotation != null
                        this.onChange = { _, checked -> onChange(signal.copy(rotation = if (checked) 0 else null)) }
                    }
                }
            }
            Grid {
                container = true
                spacing = responsive(2)
                sx {
                    alignItems = AlignItems.center
                }

                Grid {
                    item = true
                    xs = true
                    Slider {
                        disabled = signal.rotation == null
                        value = signal.rotation ?: 0
                        min = 0.0
                        max = 360.0
                        step = 1.0
                        this.onChange = { _, value, _ ->
                            onChange(signal.copy(rotation = value.toString().toInt()))
                        }
                    }
                }
                Grid {
                    item = true
                    OutlinedInput {
                        disabled = signal.rotation == null
                        value = signal.rotation ?: 0
                        size = Size.small
                        type = "number"
                        this.onChange = { event ->
                            val value = event.target.asDynamic().value.toString().toIntOrNull() ?: 0
                            val clamp = value.coerceIn(0, 360)
                            onChange(signal.copy(rotation = clamp))
                        }
                    }
                }
            }
        }
    }
}