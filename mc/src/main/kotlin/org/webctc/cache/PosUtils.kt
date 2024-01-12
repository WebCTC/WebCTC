package org.webctc.cache

import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.nbt.NBTTagCompound
import org.webctc.common.types.Pos
import org.webctc.common.types.rail.WebCTCRailPosition

class PosUtils {
    companion object {
        fun readFromNBT(tag: NBTTagCompound): Pos {
            return Pos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"))
        }
    }
}

fun Pos.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setInteger("x", this.x)
    tag.setInteger("y", this.y)
    tag.setInteger("z", this.z)
    return tag
}

fun RailPosition.toWebCTC(): WebCTCRailPosition {
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