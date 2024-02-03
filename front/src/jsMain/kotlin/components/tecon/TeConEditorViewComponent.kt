package components.tecon

import components.tecon.editor.*
import mui.material.Box
import mui.material.Card
import mui.material.CardContent
import mui.system.sx
import org.webctc.common.types.PosInt2D
import react.FC
import react.Props
import react.dom.html.ReactHTML.h1
import react.useRef
import react.useState
import web.cssom.*

external interface TeConEditorViewComponentProps : Props {

}

val TeConEditorViewComponent = FC<TeConEditorViewComponentProps> {
    var dotVisible by useState(true)
    val panzoomRef = useRef<dynamic>()

    var mode by useState("hand")

    var nowMousePos by useState(PosInt2D(0, 0))

    val setMode = { value: String? ->
        value?.let {
            if (it == "hand") {
                panzoomRef.current?.resume()
            } else {
                panzoomRef.current?.pause()
            }
            mode = it
        }
    }

    Box {
        sx {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        SvgWithDot {
            dotVisibility = dotVisible
            cursorVisibility = mode != "hand"
            onUpdateMousePos = { nowMousePos = it }
            onInitPanzoom = { panzoomRef.current = it }
        }
        Box {
            sx {
                height = 100.pct
                position = Position.relative
                display = Display.flex
                flexDirection = FlexDirection.rowReverse
            }
            Box {
                sx {
                    position = Position.absolute
                    width = 30.vw
                    height = 100.pct
                    borderRadius = 16.px
                    padding = 16.px
                }
                Card {
                    sx {
                        backgroundColor = Color("white")
                        height = 100.pct
                        overflow = Auto.auto
                    }
                    CardContent {
                        h1 { +"Editor" }
                        Box {
                            sx {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                gap = 16.px
                            }
                            Box {
                                sx {
                                    display = Display.flex
                                    gap = 8.px
                                }
                                ToggleButtonHome { panzoom = panzoomRef.current }

                                ToggleButtonGroupZoom { panzoom = panzoomRef.current }

                                ToggleButtonGroupVisibility { onChange = { dotVisible = it } }
                            }
                            ToggleButtonGroupEditMode { onChange = { setMode(it) } }
                        }
                    }
                }
            }
        }
    }
}