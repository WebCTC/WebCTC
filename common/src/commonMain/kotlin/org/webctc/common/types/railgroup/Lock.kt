package org.webctc.common.types.railgroup

data class Lock(
    val key: String,
    var frozenTime: Int = 0,
    var releaseFlag: Boolean = false,
)
