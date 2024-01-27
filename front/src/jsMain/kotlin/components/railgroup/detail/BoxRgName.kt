package components.railgroup.detail

import mui.material.Box
import mui.material.OutlinedInput
import mui.material.Paper
import react.FC
import react.Props
import react.StateInstance
import react.dom.events.ChangeEvent
import web.html.HTMLInputElement

external interface BoxRgNameProps : Props {
    var stateInstance: StateInstance<String>
}

val BoxRgName = FC<BoxRgNameProps> { props ->
    val (text, setText) = props.stateInstance

    Box {
        +"Name"
        Paper {
            OutlinedInput {
                fullWidth = true
                value = text
                onChange = {
                    val event = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                    val target = event.target
                    setText(target.value)
                }
            }
        }
    }
}