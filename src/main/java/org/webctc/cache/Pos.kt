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

    override fun hashCode(): Int {
        return (this.y + this.z * 31) * 31 + this.x
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pos

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

}
