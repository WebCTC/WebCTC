package org.webctc.cache

import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.nbt.NBTTagCompound
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.WebCTCRailPosition

fun PosInt.Companion.readFromNBT(tag: NBTTagCompound): PosInt {
    return PosInt(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"))
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