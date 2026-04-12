package components.railgroup

import mui.material.ListItemButton
import mui.material.ListItemText
import mui.system.Box
import mui.system.PropsWithSx
import mui.system.sx
import react.FC
import react.create
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.span
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px
import web.html.HTMLElement

external interface RailGroupNodeComponentProps : PropsWithSx {
    var selected: Boolean
    var onClick: (MouseEvent<HTMLElement, *>) -> Unit
    var name: String
    var count: Int
}

val RailGroupNodeComponent = FC<RailGroupNodeComponentProps> { props ->
    ListItemButton {
        sx {
            +props.sx
            marginBottom = 4.px
            marginInline = 4.px
            borderRadius = 4.px
        }
        selected = props.selected
        onClick = { props.onClick(it) }
        ListItemText {
            primary = Box.create {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }

                span { +props.name }
                span { +"${props.count} rails" }
            }
        }
    }
}
