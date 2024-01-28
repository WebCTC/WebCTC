package org.webctc.cache

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.PosInt
import org.webctc.common.types.kotlinxJson
import org.webctc.railgroup.toList
import org.webctc.railgroup.toNBTTagList
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
abstract class PosCacheData<T : Any>(mapName: String, private val clazz: KClass<T>) : WorldSavedData(mapName) {
    abstract fun getMapCache(): MutableMap<PosInt, T>

    abstract val TAG_NAME: String

    private fun fromJson(json: String): T {
        return kotlinxJson.decodeFromString(clazz.serializer(), json)
    }

    private fun toJson(json: T): String {
        return kotlinxJson.encodeToString(clazz.serializer(), json)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        getMapCache().clear()
        nbt.getTagList(TAG_NAME, 10).toList().forEach { tag ->
            val pos = PosInt.readFromNBT(tag.getCompoundTag("pos"))
            val json = tag.getString("json")
            val value = this.fromJson(json)
            getMapCache()[pos] = value
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = getMapCache().map {
            NBTTagCompound().apply {
                setTag("pos", it.key.writeToNBT())
                setString("json", toJson(it.value))
            }
        }.toNBTTagList()
        nbt.setTag(TAG_NAME, tagList)
    }
}