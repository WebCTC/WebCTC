package org.webctc.common.types.tecon.shape

import kotlin.uuid.Uuid

interface RailShape : IShape {
    val railGroupList: Set<Uuid>
}