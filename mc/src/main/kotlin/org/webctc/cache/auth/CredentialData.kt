package org.webctc.cache.auth

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.webauthn4j.authenticator.Authenticator
import com.webauthn4j.authenticator.AuthenticatorImpl
import com.webauthn4j.converter.AttestedCredentialDataConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.data.attestation.statement.AttestationStatement
import kotlinx.serialization.Serializable
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.WorldSavedData
import java.util.*

class CredentialData(mapName: String) : WorldSavedData(mapName) {
    companion object {
        var credentialCache = mutableMapOf<String, MutableList<AuthenticatorEnvelope>>()

        fun registerAuthenticator(uuid: UUID, authenticator: Authenticator) {
            val id = uuid.toString()

            credentialCache.getOrPut(id) { mutableListOf() } += AuthenticatorEnvelope(authenticator, 0)
        }

        fun searchCredential(uuid: UUID, credentialId: ByteArray): AuthenticatorEnvelope? {
            val id = uuid.toString()

            return credentialCache[id]?.find {
                it.authenticator.attestedCredentialData.credentialId.contentEquals(
                    credentialId
                )
            }
        }
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        credentialCache.clear()
        val tagList = nbt.getTagList("CredentialData", 10)
        for (i in 0 until tagList.tagCount()) {
            val tag = tagList.getCompoundTagAt(i)
            val id = tag.getString("uuid")
            val credentialList = tag.getTagList("credentials", 10)
            val authenticatorList = mutableListOf<AuthenticatorEnvelope>()
            (0 until credentialList.tagCount())
                .map { credentialList.getCompoundTagAt(it) }
                .forEach {
                    val serializedAttestedCredentialData = it.getByteArray("attestedCredentialData")
                    val serializedAttestationStatementEnvelope = it.getByteArray("attestationStatementEnvelope")
                    val signCount = it.getLong("signCount")
                    val timeStamp = it.getLong("timeStamp")

                    val objectConverter = ObjectConverter()
                    val attestedCredentialDataConverter = AttestedCredentialDataConverter(objectConverter)
                    val attestedCredentialData =
                        attestedCredentialDataConverter.convert(serializedAttestedCredentialData)
                    val attestationStatementEnvelope = objectConverter.cborConverter.readValue(
                        serializedAttestationStatementEnvelope,
                        AttestationStatementEnvelope::class.java
                    )

                    val authenticator = AuthenticatorImpl(
                        attestedCredentialData,
                        attestationStatementEnvelope?.attestationStatement,
                        signCount
                    )

                    authenticatorList += AuthenticatorEnvelope(authenticator, signCount, timeStamp)
                }
            credentialCache[id] = authenticatorList
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = NBTTagList()
        credentialCache.forEach {
            val tag = NBTTagCompound()
            tag.setString("uuid", it.key)
            val credentialList = NBTTagList()
            it.value.forEach {
                val credential = NBTTagCompound()
                val objectConverter = ObjectConverter()

                credential.setByteArray(
                    "attestedCredentialData",
                    AttestedCredentialDataConverter(objectConverter).convert(it.authenticator.attestedCredentialData)
                )

                credential.setByteArray(
                    "attestationStatementEnvelope",
                    objectConverter.cborConverter.writeValueAsBytes(
                        AttestationStatementEnvelope(it.authenticator.attestationStatement)
                    )
                )
                credential.setLong("signCount", it.signCount)

                credential.setLong("timeStamp", it.timeStamp)

                credentialList.appendTag(credential)
            }
            tag.setTag("credentials", credentialList)
            tagList.appendTag(tag)
        }
        nbt.setTag("CredentialData", tagList)
    }
}


data class AuthenticatorEnvelope(
    val authenticator: Authenticator,
    var signCount: Long,
    val timeStamp: Long = System.currentTimeMillis()
) {
    fun incrementSignCount() {
        signCount++
    }
}

@Serializable
internal class AttestationStatementEnvelope @JsonCreator constructor(
    @field:JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "fmt"
    ) @field:JsonProperty("attStmt") @param:JsonProperty(
        "attStmt"
    ) val attestationStatement: AttestationStatement?
) {
    @get:JsonProperty("fmt")
    val format: String?
        get() = attestationStatement?.format
}