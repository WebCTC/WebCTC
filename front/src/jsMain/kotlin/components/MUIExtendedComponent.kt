package components

import mui.material.Skeleton
import mui.material.SkeletonAnimation
import mui.material.SkeletonVariant
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.span
import web.cssom.Display
import web.cssom.Length
import web.cssom.rem

external interface SkeletonSpanProps : Props {
    var prefix: String
    var width: Length
    var text: String?
}


val SkeletonSpan = FC<SkeletonSpanProps> {
    span {
        +it.prefix
        +": "
        if (it.text.isNullOrEmpty()) {
            Skeleton {
                animation = SkeletonAnimation.wave
                variant = SkeletonVariant.text
                width = it.width
                sx {
                    fontSize = 1.rem
                    display = Display.inlineFlex
                }
            }
        } else {
            +it.text.toString()
        }
    }
}