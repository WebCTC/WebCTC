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
import utils.removeAtNew
import web.cssom.px

external interface DialogReceivePosProps : Props {
    var title: String
    var wsPath: String
    var uuid: UUID?
    var onSave: (list: Set<PosInt>) -> Unit
    var onClose: () -> Unit
}

val DialogReceivePos = FC<DialogReceivePosProps> { props ->
    val title = props.title
    val wsPath = props.wsPath
    val uuid = props.uuid
    val open = uuid != null
    val onSave = props.onSave
    val onClose = props.onClose

    val protocol = window.location.protocol
    val wsProtocol = if (protocol == "https:") URLProtocol.WSS else URLProtocol.WS
    val port = window.location.port.toIntOrNull() ?: wsProtocol.defaultPort

    var (data, setData) = useState(setOf<PosInt>())
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
        this.onClose = { _, _ -> onClose() }
        DialogTitle { +"Minecraft Click Handler($title)" }
        DialogContent {
            List {
                disablePadding = true
                data.forEachIndexed { index, pos ->
                    ListItem {
                        sx { paddingRight = 72.px }
                        disablePadding = true
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = { setData { it.removeAtNew(index) } }
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
            Button {
                +"Close"
                onClick = { onClose() }
            }
        }
    }
}