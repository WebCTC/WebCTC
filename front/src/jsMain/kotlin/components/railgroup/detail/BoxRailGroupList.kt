package components.railgroup.detail

import kotlinx.uuid.UUID
import mui.icons.material.Delete
import mui.material.*
import mui.system.sx
import org.webctc.common.types.railgroup.RailGroup
import react.*
import react.dom.onChange
import utils.removeAtNew
import utils.useData
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px

external interface BoxRailGroupListProps : Props {
    var title: String?
    var railGroupList: Set<UUID>
    var updateRailGroupList: (Set<UUID>) -> Unit
}

val BoxRailGroupList = FC<BoxRailGroupListProps> { props ->
    val title = props.title
    val railGroupList = props.railGroupList
    val onChange = props.updateRailGroupList

    val removeAt = { index: Int ->
        railGroupList.removeAtNew(index).also(onChange)
    }

    val add = { uuid: UUID ->
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
    var uuid: UUID
    var onDelete: () -> Unit
}

val ListItemRailGroupUUID = FC<ListItemRailGroupUUIDProps> { props ->
    val uuid = props.uuid
    val railGroup by useData<RailGroup>("/api/railgroups/$uuid")

    ListItem {
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
    var onAdd: (UUID) -> Unit
}

val ListItemRailGroupUUIDAppend = FC<ListItemRailGroupUUIDAppendProps> { props ->
    var inputValue by useState("")
    val uuid = if (UUID.isValidUUIDString(inputValue)) UUID(inputValue) else null
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
