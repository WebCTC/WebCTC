package components.railgroup.detail

import mui.material.Box
import mui.material.OutlinedInput
import mui.material.Paper
import react.FC
import react.Props
import react.dom.events.ChangeEvent
import web.html.HTMLInputElement

external interface BoxRgNameProps : Props {
    var name: String
    var onChange: (String) -> Unit
}

val BoxRgName = FC<BoxRgNameProps> { props ->
    Box {
        +"Name"
        Paper {
            OutlinedInput {
                fullWidth = true
                value = props.name
                onChange = {
                    val event = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                    val target = event.target
                    props.onChange(target.value)
                }
            }
        }
    }
}