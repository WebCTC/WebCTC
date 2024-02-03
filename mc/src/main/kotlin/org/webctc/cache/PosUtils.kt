package org.webctc.cache

import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.nbt.NBTTagCompound
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.WebCTCRailPosition
import org.webctc.common.types.railgroup.PosIntWithKey

fun PosInt.Companion.readFromNBT(tag: NBTTagCompound): PosInt {
    val x = tag.getInteger("x")
    val y = tag.getInteger("y")
    val z = tag.getInteger("z")
    return PosInt(x, y, z)
}

fun PosInt.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setInteger("x", this.x)
    tag.setInteger("y", this.y)
    tag.setInteger("z", this.z)
    return tag
}

fun RailPosition.toDataClass(): WebCTCRailPosition {
    return WebCTCRailPosition(
        this.blockX,
        this.blockY,
        this.blockZ,
        this.switchType,
        this.direction,
        this.height,
        this.posX,
        this.posY,
        this.posZ
    )
}

fun PosIntWithKey.Companion.readFromNBT(tag: NBTTagCompound): PosIntWithKey {
    val key = if (tag.hasKey("key")) tag.getString("key") else ""
    val x = tag.getInteger("x")
    val y = tag.getInteger("y")
    val z = tag.getInteger("z")
    return PosIntWithKey(x, y, z, key)
}

fun PosIntWithKey.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setInteger("x", this.x)
    tag.setInteger("y", this.y)
    tag.setInteger("z", this.z)
    this.key.takeIf { it.isNotEmpty() }?.let { tag.setString("key", it) }
    return tag
}