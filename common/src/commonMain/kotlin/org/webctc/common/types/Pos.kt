package org.webctc.common.types

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt

private const val worldSize = 30000000
private val bits = 1 + log2(ceil(worldSize.toDouble()).nextPowerOfTwo()).toInt()
private val xBits = bits
private val zBits = bits
private val yBits = 64 - xBits - zBits
private val yShift = 0 + zBits
private val xShift = yShift + yBits
private val xMask = (1L shl xBits) - 1L
private val yMask = (1L shl yBits) - 1L
private val zMask = (1L shl zBits) - 1L

@Serializable
data class PosInt(val x: Int, val y: Int, val z: Int) {
    constructor(serialized: Long) : this(
        (serialized shl 64 - xShift - xBits shr 64 - xBits).toInt(),
        (serialized shl 64 - yShift - yBits shr 64 - yBits).toInt(),
        (serialized shl 64 - zBits shr 64 - zBits).toInt()
    )

    override fun toString(): String {
        return "$x,$y,$z"
    }

    fun toLong(): Long {
        return (x.toLong() and xMask shl xShift) or (y.toLong() and yMask shl yShift) or (z.toLong() and zMask shl 0)
    }

    companion object {
        val ZERO = PosInt(0, 0, 0)
    }
}

fun Double.nextPowerOfTwo(): Double = 2.0.pow(ceil(log2(this)))

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