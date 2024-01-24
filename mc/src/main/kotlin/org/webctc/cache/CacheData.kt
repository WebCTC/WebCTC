package org.webctc.cache

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.WorldSavedData
import org.webctc.railgroup.toList
import org.webctc.railgroup.toNBTTagList

abstract class CacheData<T>(mapName: String) : WorldSavedData(mapName) {
    abstract fun getMapCache(): MutableMap<String, T>

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    abstract val TAG_NAME: String

    override fun readFromNBT(nbt: NBTTagCompound) {
        getMapCache().clear()
        nbt.getTagList(TAG_NAME, 10).toList().forEach { tag ->
            val key = tag.getString("pos")
            val json = gson.fromJson<T>(
                tag.getString("json"),
                object : TypeToken<T>() {}.type
            )
            json?.let { getMapCache()[key] = it }
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = getMapCache().map {
            NBTTagCompound().apply {
                setString("pos", it.key)
                setString("json", gson.toJson(it.value))
            }
        }.toNBTTagList()
        nbt.setTag(TAG_NAME, tagList)
    }
}