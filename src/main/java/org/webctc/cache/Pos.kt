package org.webctc.cache

import net.minecraft.nbt.NBTTagCompound


data class Pos(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun readFromNBT(tag: NBTTagCompound): Pos {
            return Pos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"))
        }
    }

    fun writeToNBT(): NBTTagCompound {
        val tag = NBTTagCompound()
        tag.setInteger("x", this.x)
        tag.setInteger("y", this.y)
        tag.setInteger("z", this.z)
        return tag
    }
}
