package org.webctc.common.types

import kotlinx.serialization.json.*

/**
 * KotlinでブラックボックスJson扱うための秘伝のタレ
 */

operator fun JsonElement.get(key: String) = this.jsonObject[key]

operator fun JsonElement.get(index: Int) = this.jsonArray[index]

fun JsonElement.string() = this.jsonPrimitive.content

fun JsonElement.getString(key: String) = this[key]!!.string()

fun JsonElement.getStringOrNull(key: String) = this[key]?.string()

fun JsonElement.int() = this.jsonPrimitive.int

fun JsonElement.getInt(key: String) = this[key]!!.int()

fun JsonElement.getIntOrNull(key: String) = this[key]?.int()

fun JsonElement.boolean() = this.jsonPrimitive.boolean

fun JsonElement.getBoolean(key: String) = this[key]!!.boolean()

fun JsonElement.getBooleanOrNull(key: String) = this[key]?.boolean()

fun JsonElement.double() = this.jsonPrimitive.double

fun JsonElement.getDouble(key: String) = this[key]!!.double()

fun JsonElement.getDoubleOrNull(key: String) = this[key]?.double()

fun JsonElement.float() = this.jsonPrimitive.float

fun JsonElement.getFloat(key: String) = this[key]!!.float()

fun JsonElement.getFloatOrNull(key: String) = this[key]?.float()

fun JsonElement.long(): Long = this.jsonPrimitive.long

fun JsonElement.getLong(key: String): Long = this[key]!!.long()

fun JsonElement.getLongOrNull(key: String): Long? = this[key]?.long()

fun JsonElement.getJsonArray(key: String) = this[key]!!.jsonArray

fun JsonElement.getJsonArrayOrNull(key: String): JsonArray? = this[key]?.jsonArray

fun JsonElement.isTrue(key: String) = this[key] != null && this[key]!!.boolean()

fun JsonElement.isFalse(key: String) = this[key] != null && !this[key]!!.boolean()

fun JsonElement.isNull(key: String) = this[key] == null

fun JsonElement.isNotNull(key: String) = this[key] != null

fun JsonArray.toList() = this.map { it.jsonObject }

fun JsonArray.toMutableList() = this.map { it.jsonObject }.toMutableList()

fun String.parseToJsonElement() = Json.parseToJsonElement(this)