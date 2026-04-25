package pages.tecon

import components.Header
import components.tecon.editor.SvgWithDot
import components.tecon.viewer.TeConViewer
import mui.material.Box
import mui.material.Card
import mui.material.CssBaseline
import mui.system.sx
import org.webctc.common.types.tecon.TeCon
import react.FC
import tanstack.react.router.useNavigate
import tanstack.react.router.useParams
import tanstack.router.core.ParamName
import tanstack.router.core.RoutePath
import utils.useData
import web.cssom.*

val TeConView = FC {
    val params = useParams()
    val uuid = params[ParamName("uuid")]
    val navigate = useNavigate()

    val tecon by useData<TeCon>("/api/tecons/$uuid") {
        navigate { to = RoutePath("/p/tecons") }

    }
    val parts = tecon?.parts

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
            if (parts == null) {
                SvgWithDot {
                    dotVisibility = false
                    cursorVisibility = false
                }
            } else {
                TeConViewer {
                    teConUuid = tecon!!.uuid.toString()
                    this.parts = parts
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
