package components.railgroup

import mui.icons.material.Add
import mui.icons.material.ArrowRight
import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.icons.material.ExpandMore
import mui.icons.material.Folder
import mui.icons.material.MoreVert
import mui.material.*
import mui.system.sx
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupFolder
import react.*
import react.dom.onChange
import web.cssom.important
import web.cssom.px
import web.dom.Element
import kotlin.uuid.Uuid

sealed class RGTreeNode {
    data class FolderNode(val folder: RailGroupFolder, val children: List<RGTreeNode>) : RGTreeNode()
    data class GroupNode(val rg: RailGroup) : RGTreeNode()
}

fun RGTreeNode.uuid(): Uuid = when (this) {
    is RGTreeNode.FolderNode -> folder.uuid
    is RGTreeNode.GroupNode -> rg.uuid
}

fun buildTree(
    folders: List<RailGroupFolder>,
    groups: List<RailGroup>,
    parentUuid: Uuid?
): List<RGTreeNode> {
    val childFolders = folders.filter { it.parentUuid == parentUuid }
    val childGroups = groups.filter { it.folderUuid == parentUuid }
    return childFolders.sortedBy { it.name }.map { f ->
        RGTreeNode.FolderNode(f, buildTree(folders, groups, f.uuid))
    } + childGroups.sortedBy { it.name }.map { RGTreeNode.GroupNode(it) }
}

external interface RailGroupTreeViewProps : Props {
    var folders: List<RailGroupFolder>
    var railGroups: List<RailGroup>
    var activeUUID: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
}

val RailGroupTreeView = FC<RailGroupTreeViewProps> { props ->
    val tree = useMemo(props.folders, props.railGroups) {
        buildTree(props.folders, props.railGroups, null)
    }

    List {
        dense = true
        disablePadding = true

        tree.forEach { node ->
            RGTreeNodeFC {
                key = Key(node.uuid().toString())
                this.node = node
                this.depth = 0
                this.activeUUID = props.activeUUID
                this.onSelectRailGroup = props.onSelectRailGroup
                this.onCreateFolder = props.onCreateFolder
                this.onRenameFolder = props.onRenameFolder
                this.onDeleteFolder = props.onDeleteFolder
            }
        }
    }
}

external interface RGTreeNodeProps : Props {
    var node: RGTreeNode
    var depth: Int
    var activeUUID: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
}

val RGTreeNodeFC: FC<RGTreeNodeProps> = FC { props ->
    when (val node = props.node) {
        is RGTreeNode.GroupNode -> {
            val rg = node.rg
            RailGroupNodeComponent {
                sx { paddingLeft = (props.depth * 16 + 24).px }
                selected = rg.uuid == props.activeUUID
                onClick = { props.onSelectRailGroup(rg.uuid) }
                name = rg.name
                count = rg.railPosList.size
            }
        }

        is RGTreeNode.FolderNode -> {
            RGFolderNodeFC {
                this.folder = node.folder
                this.children = node.children
                this.depth = props.depth
                this.activeUUID = props.activeUUID
                this.onSelectRailGroup = props.onSelectRailGroup
                this.onCreateFolder = props.onCreateFolder
                this.onRenameFolder = props.onRenameFolder
                this.onDeleteFolder = props.onDeleteFolder
            }
        }
    }
}

external interface RGFolderNodeProps : Props {
    var folder: RailGroupFolder
    var children: List<RGTreeNode>
    var depth: Int
    var activeUUID: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
}

val RGFolderNodeFC: FC<RGFolderNodeProps> = FC { props ->
    val folder = props.folder
    var anchorElement by useState<Element?>(null)
    val isOpen = anchorElement != null
    var expanded by useState(true)
    var renameDialogOpen by useState(false)
    var deleteDialogOpen by useState(false)
    var renameText by useState(folder.name)

    ListItem {
        disablePadding = true
        secondaryAction = Box.create {
            IconButton {
                onClick = { event ->
                    anchorElement = event.currentTarget
                }
                MoreVert {}
            }
            Menu {
                open = isOpen
                onClose = { anchorElement = null }
                anchorEl = { anchorElement ?: it }
                MenuItem {
                    onClick = {
                        anchorElement = null
                        props.onCreateFolder(folder.uuid)
                    }
                    Add {}
                    +"サブフォルダを追加"
                }
                MenuItem {
                    onClick = {
                        anchorElement = null
                        renameText = folder.name
                        renameDialogOpen = true
                    }
                    Edit {}
                    +"名前を変更"
                }
                MenuItem {
                    onClick = {
                        anchorElement = null
                        deleteDialogOpen = true
                    }
                    Delete {}
                    +"削除"
                }
            }
        }
        ListItemButton {
            sx {
                paddingLeft = (props.depth * 16 + 8).px
                paddingRight = important(140.px)
            }
            onClick = { expanded = !expanded }
            ListItemIcon {
                if (expanded) ExpandMore {} else ArrowRight {}
                Folder {}
            }
            ListItemText { primary = ReactNode(folder.name) }
        }
    }

    Collapse {
        `in` = expanded
        timeout = "auto"
        props.children.forEach { child ->
            RGTreeNodeFC {
                key = Key(child.uuid().toString())
                this.node = child
                this.depth = props.depth + 1
                this.activeUUID = props.activeUUID
                this.onSelectRailGroup = props.onSelectRailGroup
                this.onCreateFolder = props.onCreateFolder
                this.onRenameFolder = props.onRenameFolder
                this.onDeleteFolder = props.onDeleteFolder
            }
        }
    }

    Dialog {
        open = renameDialogOpen
        onClose = { _, _ -> renameDialogOpen = false }
        DialogTitle { +"フォルダ名の変更" }
        DialogContent {
            TextField {
                autoFocus = true
                fullWidth = true
                value = renameText
                onChange = { e -> renameText = e.target.asDynamic().value as String }
            }
        }
        DialogActions {
            Button {
                +"キャンセル"
                variant = ButtonVariant.outlined
                onClick = { renameDialogOpen = false }
            }
            Button {
                +"保存"
                variant = ButtonVariant.contained
                onClick = {
                    props.onRenameFolder(folder, renameText)
                    renameDialogOpen = false
                }
            }
        }
    }

    Dialog {
        open = deleteDialogOpen
        onClose = { _, _ -> deleteDialogOpen = false }
        DialogTitle { +"フォルダの削除" }
        DialogContent {
            DialogContentText {
                +"フォルダ「${folder.name}」を削除しますか？中のRailGroupはひとつ上のフォルダに移動されます。"
            }
        }
        DialogActions {
            Button {
                +"キャンセル"
                variant = ButtonVariant.outlined
                onClick = { deleteDialogOpen = false }
            }
            Button {
                +"削除"
                color = ButtonColor.error
                variant = ButtonVariant.contained
                onClick = {
                    props.onDeleteFolder(folder)
                    deleteDialogOpen = false
                }
            }
        }
    }
}
