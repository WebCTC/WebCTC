package org.webctc.common.types

import kotlinx.serialization.Serializable

@Serializable
data class Pos(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "$x,$y,$z"
    }

    override fun hashCode(): Int {
        return (this.y + this.z * 31) * 31 + this.x
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Pos

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }
}
