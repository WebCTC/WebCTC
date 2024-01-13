package components

import org.webctc.common.types.signal.SignalData
import react.FC
import react.Props
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.polyline
import web.cssom.Color
import web.cssom.rgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

external interface SignalProps : Props {
    var signal: SignalData
    var cx: Double
    var cy: Double
}

external interface SignalGroupProps : Props {
    var signals: List<SignalData>
}

val Signal = FC<SignalProps> {
    val signal = it.signal
    circle {
        id = "signal,${signal.pos}"
        cx = it.cx
        cy = it.cy
        r = 1.5
        fill = getSignalColor(signal.signalLevel).toString()
    }
}


val SignalGroup = FC<SignalGroupProps> {
    val signals = it.signals
    val sortedSignals = signals.sortedBy { it.pos.y }
    val baseSignal = sortedSignals.first()
    val basePosX = baseSignal.pos.x
    val basePosZ = baseSignal.pos.z
    val rotation = baseSignal.rotation
    val cos = cos((270 + rotation) * (PI / 180))
    val sin = sin((270 + rotation) * (PI / 180))

    line {
        x1 = basePosX.toDouble() - (signals.size - 1) * 3.5 * cos
        y1 = basePosZ.toDouble() + (signals.size - 1) * 3.5 * sin
        x2 = basePosX.toDouble() + 4.0 * cos
        y2 = basePosZ.toDouble() - 4.0 * sin
    }

    line {
        x1 = basePosX.toDouble() + 4.0 * cos + 1.5 * sin
        y1 = basePosZ.toDouble() + 1.5 * cos - 4.0 * sin
        x2 = basePosX.toDouble() + 4.0 * cos - 1.5 * sin
        y2 = basePosZ.toDouble() - 1.5 * cos - 4.0 * sin
    }


    sortedSignals.forEachIndexed { index, signal ->
        val blockDirectionRotation = (signal.blockDirection * 90)
        val diff = (rotation - blockDirectionRotation + 360) % 360
        val isLeft = diff > 180 && diff != 0.0.toFloat()
        val isRight = diff < 180 && diff != 0.0.toFloat()

        val offsetCx = ((if (isLeft) 3.0 else if (isRight) -3.0 else 0.0)) * sin +
                (-index * 3.5 - if (isLeft || isRight) 1.0 else 0.0) * cos
        val offsetCy = (if (isLeft) 3.0 else if (isRight) -3.0 else 0.0) * cos +
                (index * 3.5 + if (isLeft || isRight) 1.0 else 0.0) * sin

        val cxValue = signal.pos.x.toDouble() + offsetCx
        val cyValue = signal.pos.z.toDouble() + offsetCy

        if (isLeft || isRight) {
            polyline {
                fill = "none"
                points = listOf(
                    "$cxValue,$cyValue",
                    "${cxValue + 3.0 * cos},${cyValue - 3.0 * sin}",
                    "${cxValue + 3.0 * cos - (if (isLeft) 3.0 else -3.0) * sin},${cyValue - 3.0 * sin - (if (isLeft) 3.0 else -3.0) * cos}"
                ).joinToString(" ")
            }
        }

        Signal {
            this.signal = signal
            cx = cxValue
            cy = cyValue
        }
    }
}

fun getSignalColor(signalLevel: Int): Color {
    return when (signalLevel) {
        0 -> Color("darkslategray")
        1 -> rgb(2045, 0, 0)
        2 -> rgb(255, 153, 0)
        3 -> rgb(255, 204, 0)
        4 -> rgb(155, 255, 0)
        5 -> rgb(51, 204, 0)
        else -> rgb(51, 102, 255)
    }
}