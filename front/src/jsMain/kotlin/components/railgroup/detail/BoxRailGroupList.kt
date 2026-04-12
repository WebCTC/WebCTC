package components.railgroup.detail

import mui.icons.material.Delete
import mui.material.*
import mui.material.Size
import mui.system.sx
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.uuid.isValidUUIDString
import react.*
import react.dom.onChange
import utils.removeAtNew
import utils.useData
import web.cssom.*
import kotlin.uuid.Uuid

external interface BoxRailGroupListProps : Props {
    var title: String?
    var railGroupList: Set<Uuid>
    var updateRailGroupList: (Set<Uuid>) -> Unit
}

val BoxRailGroupList = FC<BoxRailGroupListProps> { props ->
    val title = props.title
    val railGroupList = props.railGroupList
    val onChange = props.updateRailGroupList

    val removeAt = { index: Int ->
        railGroupList.removeAtNew(index).also(onChange)
    }

    val add = { uuid: Uuid ->
        (railGroupList + uuid).also(onChange)
    }

    Box {
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                paddingBottom = 8.px
            }
            title?.let { +it }
        }
        Paper {
            List {
                dense = true
                disablePadding = true
                railGroupList.forEachIndexed { index, it ->
                    ListItemRailGroupUUID {
                        uuid = it
                        onDelete = { removeAt(index) }
                    }
                }
                ListItemRailGroupUUIDAppend {
                    onAdd = { add(it) }
                }
            }
        }
    }
}

external interface ListItemRailGroupUUIDProps : Props {
    var uuid: Uuid
    var onDelete: () -> Unit
}

val ListItemRailGroupUUID = FC<ListItemRailGroupUUIDProps> { props ->
    val uuid = props.uuid
    val railGroup by useData<RailGroup>("/api/railgroups/$uuid")

    ListItem {
        sx {
            padding = Padding(8.px, 0.px, 0.px, 12.px)
        }
        secondaryAction = IconButton.create {
            Delete {}
            onClick = { props.onDelete() }
        }
        ListItemText {
            primary = railGroup?.let { ReactNode(it.name) }
            secondary = ReactNode(uuid.toString())
        }
    }
}

external interface ListItemRailGroupUUIDAppendProps : Props {
    var onAdd: (Uuid) -> Unit
}

val ListItemRailGroupUUIDAppend = FC<ListItemRailGroupUUIDAppendProps> { props ->
    var inputValue by useState("")
    val uuid = if (Uuid.isValidUUIDString(inputValue)) Uuid.parse(inputValue) else null
    val railGroup by useData<RailGroup>(uuid?.let { "/api/railgroups/$it" })
    val add = {
        uuid?.let {
            props.onAdd(it)
            inputValue = ""
        }
    }

    ListItem {
        sx {
            gap = 8.px
            alignItems = AlignItems.flexStart
            paddingInline = 8.px
        }
        TextField {
            fullWidth = true
            size = Size.small
            placeholder = "RailGroup UUID"
            value = inputValue
            this.onChange = { inputValue = it.target.asDynamic().value as String }
            helperText = ReactNode(if (inputValue.isEmpty()) "" else railGroup?.name ?: "Invalid UUID")
        }

        Button {
            +"Add"
            variant = ButtonVariant.contained
            disabled = uuid == null
            onClick = { add() }
        }
    }
}
