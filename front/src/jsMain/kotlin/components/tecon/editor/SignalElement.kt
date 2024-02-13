package components.tecon.editor

import components.railgroup.detail.TextFieldPosIntXYZ
import mui.material.Box
import mui.system.sx
import org.webctc.common.types.signal.SignalState
import org.webctc.common.types.tecon.shape.Signal
import react.FC
import react.Props
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.circle
import utils.useDataWithWebsocket
import web.cssom.Display
import web.cssom.px

external interface SignalElementProps : ITeConElementProps {
    var signal: Signal
}

val SignalElement = FC<SignalElementProps> { props ->
    val signalPos = props.signal.signalPos
    val pos = props.signal.pos
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
    }
}