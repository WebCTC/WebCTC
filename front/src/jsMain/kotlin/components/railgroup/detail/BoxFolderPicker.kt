package components.railgroup.detail

import mui.material.*
import org.webctc.common.types.railgroup.RailGroupFolder
import react.FC
import react.Props
import react.dom.events.ChangeEvent
import web.html.HTMLSelectElement
import kotlin.uuid.Uuid

external interface BoxFolderPickerProps : Props {
    var folders: List<RailGroupFolder>
    var currentFolderUuid: Uuid?
    var onChange: (Uuid?) -> Unit
}

fun List<RailGroupFolder>.fullPath(folder: RailGroupFolder): String {
    val parts = mutableListOf(folder.name)
    var current = folder
    while (true) {
        val parent = current.parentUuid?.let { pid -> find { it.uuid == pid } } ?: break
        parts.add(0, parent.name)
        current = parent
    }
    return parts.joinToString(separator = " / ", prefix = "/ ")
}

val BoxFolderPicker = FC<BoxFolderPickerProps> { props ->
    val folders = props.folders.sortedBy { props.folders.fullPath(it) }
    val currentValue = props.currentFolderUuid?.toString() ?: "root"

    Box {
        +"Folder"
        Paper {
            Select {
                value = currentValue
                size = Size.small
                fullWidth = true
                onChange = { event, _ ->
                    val event = event.unsafeCast<ChangeEvent<HTMLSelectElement, HTMLSelectElement>>()
                    val v = event.target.value
                    props.onChange(if (v == "root") null else Uuid.parse(v))
                }
                MenuItem {
                    value = "root"
                    +"/"
                }
                folders.forEach { f ->
                    MenuItem {
                        value = f.uuid.toString()
                        +folders.fullPath(f)
                    }
                }
            }
        }
    }
}
