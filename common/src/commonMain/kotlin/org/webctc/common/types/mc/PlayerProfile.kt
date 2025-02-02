package org.webctc.common.types.mc

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlin.uuid.Uuid

@Serializable
data class PlayerProfile(
    @JsonNames("uuid_formatted")
    @OptIn(ExperimentalSerializationApi::class)
    val uuid: Uuid,
    val username: String
)
