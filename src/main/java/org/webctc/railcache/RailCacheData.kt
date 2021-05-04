package org.webctc.railcache

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.WorldSavedData

class RailCacheData(mapName: String) : WorldSavedData(mapName) {
    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    override fun readFromNBT(nbt: NBTTagCompound) {
        val tagList = nbt.getTagList("RailCache", 10)
        for (i in 0 until tagList.tagCount()) {
            val tag = tagList.getCompoundTagAt(i)
            val pos = RailCache.Pos.readFromNBT(tag)
            val json =
                gson.fromJson<MutableMap<String, Any?>>(
                    tag.getString("json"),
                    object : TypeToken<MutableMap<String, Any?>>() {}.type
                )
            json?.let { RailCache.railCoreMapCache[pos] = it }
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = NBTTagList()
        RailCache.railCoreMapCache.forEach {
            tagList.appendTag(it.key.writeToNBT())
            val tag = NBTTagCompound()
            tag.setString("json", gson.toJson(it.value))
            tagList.appendTag(tag)
        }
        nbt.setTag("RailCache", tagList)
    }
}