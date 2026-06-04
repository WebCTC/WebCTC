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
import web.cssom.rgb
import web.dom.Element
import kotlin.uuid.Uuid

// dragover時はdataTransfer.getData()が読めないため、モジュール変数で追跡する
private var currentDrag: Pair<String, Uuid>? = null

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

fun flattenTree(nodes: List<RGTreeNode>): List<RGTreeNode> =
    nodes.flatMap { node ->
        when (node) {
            is RGTreeNode.FolderNode -> listOf(node) + flattenTree(node.children)
            is RGTreeNode.GroupNode -> listOf(node)
        }
    }

fun isDescendantOf(folders: List<RailGroupFolder>, ancestorUuid: Uuid, targetUuid: Uuid): Boolean {
    var current = folders.find { it.uuid == targetUuid }
    while (current != null) {
        if (current.uuid == ancestorUuid) return true
        val parentId = current.parentUuid ?: return false
        current = folders.find { it.uuid == parentId }
    }
    return false
}

external interface RailGroupTreeViewProps : Props {
    var folders: List<RailGroupFolder>
    var railGroups: List<RailGroup>
    var activeUUID: Uuid?
    var selectedUUIDs: Set<Uuid>
    var lastClickedUuid: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onToggleSelect: (Uuid) -> Unit
    var onRangeSelect: (from: Uuid, to: Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
    var onMoveFolders: (uuids: Set<Uuid>, newParentUuid: Uuid?) -> Unit
    var onMoveRailGroups: (uuids: Set<Uuid>, newFolderUuid: Uuid?) -> Unit
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
                this.folders = props.folders
                this.activeUUID = props.activeUUID
                this.selectedUUIDs = props.selectedUUIDs
                this.lastClickedUuid = props.lastClickedUuid
                this.onSelectRailGroup = props.onSelectRailGroup
                this.onToggleSelect = props.onToggleSelect
                this.onRangeSelect = props.onRangeSelect
                this.onCreateFolder = props.onCreateFolder
                this.onRenameFolder = props.onRenameFolder
                this.onDeleteFolder = props.onDeleteFolder
                this.onMoveFolders = props.onMoveFolders
                this.onMoveRailGroups = props.onMoveRailGroups
            }
        }
    }

}

external interface RGTreeNodeProps : Props {
    var node: RGTreeNode
    var depth: Int
    var folders: List<RailGroupFolder>
    var activeUUID: Uuid?
    var selectedUUIDs: Set<Uuid>
    var lastClickedUuid: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onToggleSelect: (Uuid) -> Unit
    var onRangeSelect: (from: Uuid, to: Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
    var onMoveFolders: (uuids: Set<Uuid>, newParentUuid: Uuid?) -> Unit
    var onMoveRailGroups: (uuids: Set<Uuid>, newFolderUuid: Uuid?) -> Unit
}

val RGTreeNodeFC: FC<RGTreeNodeProps> = FC { props ->
    when (val node = props.node) {
        is RGTreeNode.GroupNode -> {
            val rg = node.rg
            var isDragOver by useState(false)
            Box {
                sx { asDynamic().overflow = "hidden" }
                draggable = true
                onDragStart = { e ->
                    currentDrag = "railgroup" to rg.uuid
                    e.dataTransfer.asDynamic().effectAllowed = "move"
                }
                onDragEnd = { currentDrag = null }
                onDragOver = { e ->
                    val drag = currentDrag
                    if (drag != null && drag.second != rg.uuid) {
                        e.preventDefault()
                        e.stopPropagation()
                        isDragOver = true
                    }
                }
                onDragLeave = { isDragOver = false }
                onDrop = { e ->
                    e.preventDefault()
                    isDragOver = false
                    val drag = currentDrag
                    currentDrag = null
                    if (drag != null && drag.second != rg.uuid) {
                        val targetFolder = rg.folderUuid
                        val uuids = if (drag.second in props.selectedUUIDs) props.selectedUUIDs else setOf(drag.second)
                        val folderUuids = uuids.filter { uid -> props.folders.any { it.uuid == uid } }.toSet()
                        val rgUuids = uuids.filter { uid -> props.folders.none { it.uuid == uid } }.toSet()
                        if (folderUuids.isNotEmpty()) props.onMoveFolders(folderUuids, targetFolder)
                        if (rgUuids.isNotEmpty()) props.onMoveRailGroups(rgUuids, targetFolder)
                    }
                }
                RailGroupNodeComponent {
                    sx {
                        paddingLeft = (props.depth * 16 + 24).px
                        if (isDragOver) backgroundColor = rgb(25, 118, 210, 0.12)
                    }
                    selected = rg.uuid == props.activeUUID || rg.uuid in props.selectedUUIDs
                    onClick = { e ->
                        when {
                            e.ctrlKey || e.metaKey -> props.onToggleSelect(rg.uuid)
                            e.shiftKey -> {
                                val last = props.lastClickedUuid
                                if (last != null) props.onRangeSelect(last, rg.uuid)
                                else props.onToggleSelect(rg.uuid)
                            }

                            else -> props.onSelectRailGroup(rg.uuid)
                        }
                    }
                    name = rg.name
                    count = rg.railPosList.size
                }
            }
        }

        is RGTreeNode.FolderNode -> {
            RGFolderNodeFC {
                this.folder = node.folder
                this.children = node.children
                this.depth = props.depth
                this.folders = props.folders
                this.activeUUID = props.activeUUID
                this.selectedUUIDs = props.selectedUUIDs
                this.lastClickedUuid = props.lastClickedUuid
                this.onSelectRailGroup = props.onSelectRailGroup
                this.onToggleSelect = props.onToggleSelect
                this.onRangeSelect = props.onRangeSelect
                this.onCreateFolder = props.onCreateFolder
                this.onRenameFolder = props.onRenameFolder
                this.onDeleteFolder = props.onDeleteFolder
                this.onMoveFolders = props.onMoveFolders
                this.onMoveRailGroups = props.onMoveRailGroups
            }
        }
    }
}

external interface RGFolderNodeProps : Props {
    var folder: RailGroupFolder
    var children: List<RGTreeNode>
    var depth: Int
    var folders: List<RailGroupFolder>
    var activeUUID: Uuid?
    var selectedUUIDs: Set<Uuid>
    var lastClickedUuid: Uuid?
    var onSelectRailGroup: (Uuid) -> Unit
    var onToggleSelect: (Uuid) -> Unit
    var onRangeSelect: (from: Uuid, to: Uuid) -> Unit
    var onCreateFolder: (parentUuid: Uuid?) -> Unit
    var onRenameFolder: (folder: RailGroupFolder, newName: String) -> Unit
    var onDeleteFolder: (folder: RailGroupFolder) -> Unit
    var onMoveFolders: (uuids: Set<Uuid>, newParentUuid: Uuid?) -> Unit
    var onMoveRailGroups: (uuids: Set<Uuid>, newFolderUuid: Uuid?) -> Unit
}

val RGFolderNodeFC: FC<RGFolderNodeProps> = FC { props ->
    val folder = props.folder
    var anchorElement by useState<Element?>(null)
    val isOpen = anchorElement != null
    var expanded by useState(false)
    var renameDialogOpen by useState(false)
    var deleteDialogOpen by useState(false)
    var renameText by useState(folder.name)
    var isDragOver by useState(false)

    val isSelected = folder.uuid in props.selectedUUIDs

    fun resolveUuidsForDrop(): Pair<Set<Uuid>, Set<Uuid>>? {
        val drag = currentDrag ?: return null
        val all = if (drag.second in props.selectedUUIDs) props.selectedUUIDs else setOf(drag.second)
        val folderUuids = all.filter { uid -> props.folders.any { it.uuid == uid } }.toSet()
        val rgUuids = all.filter { uid -> props.folders.none { it.uuid == uid } }.toSet()
        return folderUuids to rgUuids
    }

    Box {
        onDragOver = { e ->
            val drag = currentDrag
            if (drag != null) {
                val valid = when (drag.first) {
                    "folder" -> drag.second != folder.uuid &&
                            !isDescendantOf(props.folders, drag.second, folder.uuid)

                    "railgroup" -> true
                    else -> false
                }
                if (valid) {
                    e.preventDefault()
                    isDragOver = true
                }
            }
        }
        onDragLeave = { e ->
            val related = e.relatedTarget
            val self = e.currentTarget
            if (related == null || !self.contains(related.unsafeCast<web.dom.Node>())) {
                isDragOver = false
            }
        }
        onDrop = { e ->
            e.preventDefault()
            isDragOver = false
            val resolved = resolveUuidsForDrop()
            currentDrag = null
            if (resolved != null) {
                val (folderUuids, rgUuids) = resolved
                if (folderUuids.isNotEmpty()) props.onMoveFolders(folderUuids, folder.uuid)
                if (rgUuids.isNotEmpty()) props.onMoveRailGroups(rgUuids, folder.uuid)
            }
        }

        Box {
            draggable = true
            onDragStart = { e ->
                currentDrag = "folder" to folder.uuid
                e.dataTransfer.asDynamic().effectAllowed = "move"
            }
            onDragEnd = { currentDrag = null }

            ListItem {
                disablePadding = true
                sx {
                    if (isDragOver) backgroundColor = rgb(25, 118, 210, 0.15)
                    else if (isSelected) backgroundColor = rgb(25, 118, 210, 0.08)
                }
                secondaryAction = Box.create {
                    IconButton {
                        onClick = { event -> anchorElement = event.currentTarget }
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
                    onClick = { e ->
                        when {
                            e.ctrlKey || e.metaKey -> props.onToggleSelect(folder.uuid)
                            e.shiftKey -> {
                                val last = props.lastClickedUuid
                                if (last != null) props.onRangeSelect(last, folder.uuid)
                                else props.onToggleSelect(folder.uuid)
                            }

                            else -> expanded = !expanded
                        }
                    }
                    ListItemIcon {
                        if (expanded) ExpandMore {} else ArrowRight {}
                        Folder {}
                    }
                    ListItemText { primary = ReactNode(folder.name) }
                }
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
                    this.folders = props.folders
                    this.activeUUID = props.activeUUID
                    this.selectedUUIDs = props.selectedUUIDs
                    this.lastClickedUuid = props.lastClickedUuid
                    this.onSelectRailGroup = props.onSelectRailGroup
                    this.onToggleSelect = props.onToggleSelect
                    this.onRangeSelect = props.onRangeSelect
                    this.onCreateFolder = props.onCreateFolder
                    this.onRenameFolder = props.onRenameFolder
                    this.onDeleteFolder = props.onDeleteFolder
                    this.onMoveFolders = props.onMoveFolders
                    this.onMoveRailGroups = props.onMoveRailGroups
                }
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
