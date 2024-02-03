package components.tecon.editor

import emotion.react.css
import js.objects.jso
import org.webctc.common.types.PosInt2D
import react.*
import react.dom.events.MouseEvent
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.svg
import web.cssom.Color
import web.cssom.number
import web.cssom.px
import web.cssom.url
import web.svg.SVGElement
import web.svg.SVGSVGElement

external interface SvgWithDotProps : PropsWithChildren {
    var dotVisibility: Boolean
    var cursorVisibility: Boolean
    var onClick: (MouseEvent<SVGSVGElement, *>) -> Unit
    var onUpdateMousePos: (PosInt2D) -> Unit
    var onInitPanzoom: (dynamic) -> Unit
}

val dotGap = 32

val SvgWithDot = FC<SvgWithDotProps> { props ->
    var scale by useState(0.0)
    val scaledDotGap = dotGap * scale
    var offsetX by useState(0.0)
    var offsetY by useState(0.0)

    val dotVisibility = props.dotVisibility
    val cursorVisibility = props.cursorVisibility
    var mousePos by useState(PosInt2D(0, 0))

    val tcnRef = useRef<SVGElement>()
    val panzoomRef = useRef<dynamic>()

    useLayoutEffectOnce {
        val panzoom = module.panzoom(tcnRef.current!!, jso { smoothScroll = false })
        panzoom.on("transform") { e: dynamic ->
            val transform = e.getTransform()
            transform.scale.toString().toDouble().also {
                scale = it
                val scaledBox = dotGap * it
                offsetX = transform.x.toString().toDouble() % scaledBox
                offsetY = transform.y.toString().toDouble() % scaledBox
            }
        }
        panzoomRef.current = panzoom
        props.onInitPanzoom(panzoom)
    }


    svg {
        css {
            flexGrow = number(1.0)
            backgroundColor = Color("#202020")
            if (dotVisibility) {
                backgroundImage =
                    url("data:image/svg+xml;charset=utf8,%3Csvg%20width%3D%2232%22%20height%3D%2232%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%20%3Ccircle%20cx%3D%2216%22%20cy%3D%2216%22%20r%3D%221%22%20fill%3D%22gray%22%2F%3E%3C%2Fsvg%3E")
                backgroundSize = scaledDotGap.px
                backgroundPositionX = (scaledDotGap / 2 + offsetX).px
                backgroundPositionY = (scaledDotGap / 2 + offsetY).px
            }
        }
        onClick = { e -> props.onClick(e) }
        onMouseMove = { e ->
            val rect = e.currentTarget.getBoundingClientRect()
            val x = e.clientX - rect.left
            val y = e.clientY - rect.top

            val transform = panzoomRef.current!!.getTransform()
            val scale = transform.scale.toString().toDouble()
            val svgX = transform.x.toString().toDouble()
            val svgY = transform.y.toString().toDouble()

            val mouseXSvg = x / scale - svgX / scale
            val mouseYSvg = y / scale - svgY / scale

            val dotGapX = if (mouseXSvg < 0) -dotGap else dotGap
            val dotGapY = if (mouseYSvg < 0) -dotGap else dotGap

            val mouseX = mouseXSvg + dotGapX / 2 - (mouseXSvg + dotGapX / 2) % dotGapX
            val mouseY = mouseYSvg + dotGapY / 2 - (mouseYSvg + dotGapY / 2) % dotGapY

            val pos = PosInt2D(mouseX.toInt(), mouseY.toInt())
            mousePos = pos
            props.onUpdateMousePos(pos)
        }
        g {
            ref = tcnRef
            if (cursorVisibility) {
                CircleMousePos { pos = mousePos }
            }
            +props.children
        }
    }
}