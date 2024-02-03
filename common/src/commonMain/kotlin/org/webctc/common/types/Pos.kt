package org.webctc.common.types

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class PosInt(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "$x,$y,$z"
    }

    companion object {
        val ZERO = PosInt(0, 0, 0)
    }
}

fun IntArray.toPosInt(): PosInt {
    return PosInt(this[0], this[1], this[2])
}

@Serializable
data class PosInt2D(val x: Int, val y: Int) {
    override fun toString(): String {
        return "$x,$y"
    }
}

@Serializable
data class PosDouble(val x: Double, val y: Double, val z: Double) {
    fun distanceTo(other: PosDouble): Double {
        val dx = x - other.x
        val dz = z - other.z
        return sqrt(dx * dx + dz * dz)
    }

    fun isInsideSegment2D(b1: PosDouble, b2: PosDouble): Boolean {
        val ax = x - b1.x
        val az = z - b1.z
        val bx = b2.x - b1.x
        val bz = b2.z - b1.z
        val r = (ax * bx + az * bz) / (bx * bx + bz * bz)

        return r in 0.0..1.0
    }

    fun distanceToSegment(b1: PosDouble, b2: PosDouble): Double {
        val dx = b2.x - b1.x
        val dz = b2.z - b1.z
        val r = ((x - b1.x) * dx + (z - b1.z) * dz) / (dx * dx + dz * dz)

        return if (r in 0.0..1.0) {
            val closestX = b1.x + r * dx
            val closestZ = b1.z + r * dz
            sqrt((x - closestX) * (x - closestX) + (z - closestZ) * (z - closestZ))
        } else {
            Double.MAX_VALUE
        }
    }

    override fun toString(): String {
        return "$x,$y,$z"
    }

    companion object {
        val ZERO: PosDouble = PosDouble(0.0, 0.0, 0.0)
    }
}