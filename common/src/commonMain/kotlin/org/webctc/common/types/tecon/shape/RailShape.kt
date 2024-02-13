package org.webctc.common.types.tecon.shape

import kotlinx.uuid.UUID

interface RailShape : IShape {
    val railGroupList: Set<UUID>
}