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

    override fun equals(other: Any?): Boolean {
        return other is Pos && this.x == other.x && this.y == other.y && this.z == other.z
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }
}
