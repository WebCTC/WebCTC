package org.webctc.cache

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.WorldSavedData
import org.webctc.kotlinxJson
import org.webctc.railgroup.toList
import org.webctc.railgroup.toNBTTagList
import kotlin.reflect.KClass

abstract class CacheData<T : Any>(mapName: String, private val clazz: KClass<T>) : WorldSavedData(mapName) {
    abstract fun getMapCache(): MutableMap<String, T>

    abstract val TAG_NAME: String

    @OptIn(InternalSerializationApi::class)
    private fun fromJson(json: String): T {
        return kotlinxJson.decodeFromString(clazz.serializer(), json)
    }

    @OptIn(InternalSerializationApi::class)
    private fun toJson(json: T): String {
        return kotlinxJson.encodeToString(clazz.serializer(), json)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        getMapCache().clear()
        nbt.getTagList(TAG_NAME, 10).toList().forEach { tag ->
            val key = tag.getString("pos")
            val json = tag.getString("json")
            val value = fromJson(json)
            getMapCache()[key] = value
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = getMapCache().map {
            NBTTagCompound().apply {
                setString("pos", it.key)
                setString("json", toJson(it.value))
            }
        }.toNBTTagList()
        nbt.setTag(TAG_NAME, tagList)
    }
}