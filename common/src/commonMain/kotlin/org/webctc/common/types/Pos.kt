package org.webctc.common.types

import kotlinx.serialization.Serializable

@Serializable
data class PosInt(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "$x,$y,$z"
    }
}

@Serializable
data class PosDouble(val x: Double, val y: Double, val z: Double) {
    override fun toString(): String {
        return "$x,$y,$z"
    }
}
