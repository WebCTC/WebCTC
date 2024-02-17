package org.webctc.cache.tecon

import kotlinx.uuid.UUID
import kotlinx.uuid.toUUID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.kotlinxJson
import org.webctc.common.types.tecon.TeCon
import org.webctc.railgroup.toList
import org.webctc.railgroup.toNBTTagList

class TeConData(mapName: String) : WorldSavedData(mapName) {
    companion object {
        var teConList = mutableMapOf<UUID, TeCon>()

        fun create(): TeCon {
            val teCon = TeCon()
            teConList[teCon.uuid] = teCon
            return teCon
        }
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        teConList = nbt.getTagList("teConList", 10)
            .toList()
            .associate {
                val uuid = it.getString("uuid").toUUID()
                val teCon = kotlinxJson.decodeFromString(TeCon.serializer(), it.getString("json"))
                uuid to teCon
            }.toMutableMap()
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        teConList
            .map {
                NBTTagCompound().apply {
                    setString("uuid", it.key.toString())
                    setString("json", kotlinxJson.encodeToString(TeCon.serializer(), it.value))
                }
            }.toNBTTagList()
            .let { nbt.setTag("teConList", it) }
    }
}

fun TeCon.delete(): Boolean {
    return TeConData.teConList.remove(this.uuid) != null
}