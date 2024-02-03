package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class SwitchSetting(
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
        return other is SettingEntry && this.key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    companion object
}