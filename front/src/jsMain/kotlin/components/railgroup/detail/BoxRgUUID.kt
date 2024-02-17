package components.railgroup.detail

import js.objects.jso
import kotlinx.uuid.UUID
import mui.icons.material.ContentCopy
import mui.material.*
import react.FC
import react.Props
import react.create
import react.dom.aria.ariaReadOnly
import web.navigator.navigator

external interface BoxRgUUIDProps : Props {
    var uuid: UUID
}

val BoxRgUUID = FC<BoxRgUUIDProps> { props ->
    val uuid = props.uuid

    Box {
        +"UUID"
        Paper {
            OutlinedInput {
                fullWidth = true
                value = uuid.toString()
                inputProps = jso { ariaReadOnly = true }
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    IconButton {
                        ContentCopy {}
                        onClick = { navigator.clipboard.writeText(uuid.toString()) }
                    }
                }
            }
        }
    }
}