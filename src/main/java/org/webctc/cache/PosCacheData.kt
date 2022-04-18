package org.webctc.cache

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.WorldSavedData

abstract class PosCacheData<T>(mapName: String) : WorldSavedData(mapName) {
    abstract fun getMapCache(): MutableMap<Pos, T>

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    abstract val TAG_NAME: String

    override fun readFromNBT(nbt: NBTTagCompound) {
        getMapCache().clear()
        val tagList = nbt.getTagList(TAG_NAME, 10)
        for (i in 0 until tagList.tagCount()) {
            val tag = tagList.getCompoundTagAt(i)
            val pos = Pos.readFromNBT(tag.getCompoundTag("pos"))
            val json =
                gson.fromJson<T>(
                    tag.getString("json"),
                    object : TypeToken<T>() {}.type
                )
            json?.let { getMapCache()[pos] = it }
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = NBTTagList()
        getMapCache().forEach {
            val tag = NBTTagCompound()
            tag.setTag("pos", it.key.writeToNBT())
            tag.setString("json", gson.toJson(it.value))
            tagList.appendTag(tag)
        }
        nbt.setTag(TAG_NAME, tagList)
    }
}