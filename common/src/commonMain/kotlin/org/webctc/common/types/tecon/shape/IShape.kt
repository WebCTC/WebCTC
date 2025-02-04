package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Polymorphic
import org.webctc.common.types.PosInt2D

@Polymorphic
interface IShape {
    val pos: PosInt2D
    val zIndex: Int
}