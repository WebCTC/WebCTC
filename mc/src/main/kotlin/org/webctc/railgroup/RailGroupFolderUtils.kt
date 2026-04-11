package org.webctc.railgroup

import net.minecraft.nbt.NBTTagCompound
import org.webctc.common.types.railgroup.RailGroupFolder
import kotlin.uuid.Uuid

fun RailGroupFolder.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setString("uuid", uuid.toString())
    tag.setString("name", name)
    parentUuid?.let { tag.setString("parentUuid", it.toString()) }
    return tag
}

fun RailGroupFolder.Companion.readFromNBT(tag: NBTTagCompound): RailGroupFolder {
    val uuid = Uuid.parse(tag.getString("uuid"))
    val name = tag.getString("name")
    val parentUuid = if (tag.hasKey("parentUuid")) Uuid.parse(tag.getString("parentUuid")) else null
    return RailGroupFolder(uuid, name, parentUuid)
}

fun RailGroupFolder.Companion.create(): RailGroupFolder {
    val folder = RailGroupFolder()
    RailGroupData.folderList.add(folder)
    return folder
}

fun RailGroupFolder.delete(): Boolean = RailGroupData.folderList.remove(this)
