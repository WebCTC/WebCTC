package components.railgroup.detail

import client
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt
import react.*
import web.cssom.px

external interface DialogReceivePosProps : Props {
    var title: String
    var wsPath: String
    var uuid: UUID?
    var onSave: (list: List<PosInt>) -> Unit
}

val DialogReceivePos = FC<DialogReceivePosProps> { props ->
    val title = props.title
    val wsPath = props.wsPath
    val uuid = props.uuid
    val open = uuid != null
    val onSave = props.onSave

    val protocol = window.location.protocol
    val wsProtocol = if (protocol == "https:") URLProtocol.WSS else URLProtocol.WS
    val port = window.location.port.toIntOrNull() ?: wsProtocol.defaultPort

    val (data, setData) = useState(listOf<PosInt>())
    var session by useState<WebSocketSession?>(null)

    useEffect(open) {
        if (!open) {
            return@useEffect
        }
        MainScope().launch {
            client.ws(wsPath, {
                url.protocol = wsProtocol
                url.port = port
            }) {
                session = this
                while (true) {
                    val received = receiveDeserialized<PosInt>()
                    setData { it + received }
                }
            }
        }
        cleanup {
            MainScope().launch {
                session?.close()
            }
        }
    }

    Dialog {
        this.open = open
        DialogTitle { +"Minecraft Click Handler($title)" }
        DialogContent {
            List {
                disablePadding = true
                data.forEach { pos ->
                    ListItem {
                        sx { paddingRight = 72.px }
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = { setData { it - pos } }
                        }

                        ListItemPosInt {
                            this.pos = pos
                        }
                    }
                }
            }
        }
        DialogActions {
            Button {
                +"Save"
                onClick = { onSave(data) }
            }
        }
    }
}