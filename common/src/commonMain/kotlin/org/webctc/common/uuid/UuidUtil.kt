package org.webctc.common.uuid

import kotlin.uuid.Uuid

val uuidReg = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

fun Uuid.Companion.isValidUUIDString(uuidString: String): Boolean {
    return uuidReg.matches(uuidString)
}