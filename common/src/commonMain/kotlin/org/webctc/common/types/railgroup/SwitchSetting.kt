package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class SwitchSetting(
    var name: String = "Default Name",
    var switchRsPos: Set<PosInt> = setOf(),
    var settingMap: Set<SettingEntry> = setOf()
) {
    companion object
}

@Serializable
data class SettingEntry(
    var key: String = "",
    var value: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SettingEntry) return false

        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    companion object
}