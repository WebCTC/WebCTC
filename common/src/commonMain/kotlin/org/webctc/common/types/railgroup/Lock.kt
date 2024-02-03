package org.webctc.common.types.railgroup

data class Lock(
    val key: String,
    var releaseFlag: Boolean = false
)
