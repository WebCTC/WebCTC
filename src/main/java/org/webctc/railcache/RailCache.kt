package org.webctc.railcache

import net.minecraft.nbt.NBTTagCompound

class RailCache {
    companion object {
        var railCoreMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

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
}