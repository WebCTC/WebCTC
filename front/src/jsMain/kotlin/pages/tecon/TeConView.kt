package pages.tecon

import components.Header
import components.tecon.editor.RailLineElement
import components.tecon.editor.SignalElement
import components.tecon.editor.SvgWithDot
import mui.material.Box
import mui.material.Card
import mui.material.CssBaseline
import mui.system.sx
import org.webctc.common.types.tecon.TeCon
import org.webctc.common.types.tecon.shape.RailLine
import org.webctc.common.types.tecon.shape.Signal
import react.FC
import react.router.useNavigate
import react.router.useParams
import utils.useData
import web.cssom.*

val TeConView = FC {
    val params = useParams()
    val uuid = params["uuid"]
    val navigate = useNavigate()

    val tecon by useData<TeCon>("/api/tecons/$uuid") {
        navigate("/p/tecons")
    }
    val parts = tecon?.parts ?: emptyList()

    CssBaseline {}

    Box {
        sx {
            height = 100.vh
            display = Display.flex
            flexDirection = FlexDirection.column
        }

        Header {}
        Box {
            sx {
                height = 100.pct
                display = Display.flex
                flexDirection = FlexDirection.row
            }
            SvgWithDot {
                dotVisibility = false
                cursorVisibility = false

                parts.forEach {
                    when (it) {
                        is RailLine -> RailLineElement {
                            rail = it
                        }

                        is Signal -> SignalElement {
                            signal = it
                        }

                        else -> {}
                    }
                }
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
                        height = 100.pct
                        borderRadius = 16.px
                        padding = 16.px
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        gap = 16.px
                    }
                    Card {
                        sx {
                            backgroundColor = Color("rgba(255,255,255,0.4)")
                            overflow = Auto.auto
                        }
                        Box {
                            sx {
                                color = Color("White")
                                padding = 16.px
                                whiteSpace = WhiteSpace.nowrap
                            }

                            +(tecon?.name ?: "No data")
                        }
                    }
                }
            }
        }
    }
}