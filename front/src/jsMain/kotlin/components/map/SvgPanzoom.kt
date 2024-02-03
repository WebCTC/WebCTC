package components.map

import js.objects.jso
import module.panzoom
import pages.MapSVG
import react.FC
import react.PropsWithChildren
import react.dom.svg.ReactSVG.g
import react.useLayoutEffectOnce
import react.useRef
import web.svg.SVGElement

val MapPanzoomSvg = FC<PropsWithChildren> { props ->

    val mtxRef = useRef<SVGElement>()

    useLayoutEffectOnce { panzoom(mtxRef.current!!, jso { smoothScroll = false }) }

    MapSVG {
        g {
            ref = mtxRef
            +props.children
        }
    }
}