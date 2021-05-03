package org.webctc.railcache

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.storage.WorldSavedData

class RailCacheData(mapName: String) : WorldSavedData(mapName) {
    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    override fun readFromNBT(nbt: NBTTagCompound) {
        val tagList = nbt.getTagList("RailCache", 10)
        for (i in 0 until tagList.tagCount()) {
            val tag = tagList.getCompoundTagAt(i)
            val pos = BlockPos.fromLong(tag.getLong("blockPos"))
            val json =
                gson.fromJson<MutableMap<String, Any?>>(
                    tag.getString("json"),
                    object : TypeToken<MutableMap<String, Any?>>() {}.type
                )
            RailCache.railCoreMapCache[pos] = json
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound {
        val tagList = NBTTagList()
        RailCache.railCoreMapCache.forEach {
            val tag = NBTTagCompound()
            tag.setLong("blockPos", it.key.toLong())
            tag.setString("json", gson.toJson(it.value))
            tagList.appendTag(tag)
        }
        nbt.setTag("RailCache", tagList)
        return nbt
    }
}