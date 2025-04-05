package org.webctc.cache.tecon

import kotlinx.serialization.PolymorphicSerializer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagString
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.kotlinxJson
import org.webctc.common.types.tecon.TeCon
import org.webctc.common.types.tecon.shape.IShape
import org.webctc.railgroup.toList
import org.webctc.railgroup.toNBTTagList
import org.webctc.railgroup.toStringList
import kotlin.uuid.Uuid

class TeConData(mapName: String) : WorldSavedData(mapName) {
    companion object {
        var teConList = mutableMapOf<Uuid, TeCon>()

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
                val uuid = Uuid.parse(it.getString("uuid"))
                val teCon = if (it.hasKey("json")) {
                    kotlinxJson.decodeFromString(TeCon.serializer(), it.getString("json"))
                } else {
                    TeCon.readFromNBT(it.getCompoundTag("data"))
                }
                uuid to teCon
            }.toMutableMap()
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        teConList
            .map {
                NBTTagCompound().apply {
                    setString("uuid", it.key.toString())
                    setTag("data", it.value.writeToNBT())
                }
            }.toNBTTagList()
            .let { nbt.setTag("teConList", it) }
    }
}

fun TeCon.delete(): Boolean {
    return TeConData.teConList.remove(this.uuid) != null
}

private fun TeCon.writeToNBT(): NBTTagCompound {
    val nbt = NBTTagCompound()
    nbt.setString("uuid", this.uuid.toString())
    nbt.setString("name", this.name)
    this.parts.map {
        kotlinxJson.encodeToString(PolymorphicSerializer(IShape::class), it)
    }.map(::NBTTagString).toNBTTagList().let {
        nbt.setTag("parts", it)
    }
    return nbt
}

private fun TeCon.Companion.readFromNBT(nbt: NBTTagCompound): TeCon {
    val uuid = Uuid.parse(nbt.getString("uuid"))
    val name = nbt.getString("name")
    val parts = nbt.getTagList("parts", 8)
        .toStringList()
        .map { kotlinxJson.decodeFromString(PolymorphicSerializer(IShape::class), it) }
    return TeCon(uuid, name, parts)
}