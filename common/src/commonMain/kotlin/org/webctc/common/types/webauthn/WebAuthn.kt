package org.webctc.common.types.webauthn

import kotlinx.serialization.Serializable

@Serializable
data class WebAuthnRegistrationOption(
    val challenge: String,
    val rp: WebAuthnRP,
    val user: WebAuthnUser,
    val pubKeyCredParams: List<WebAuthnPubKeyCredParams>,
    val timeout: Int = 60000,
)

@Serializable
data class WebAuthnAuthenticationOption(
    val challenge: String,
    val rpId: String,
    val userVerification: String = "preferred",
    val timeout: Int = 60000,
)

@Serializable
data class WebAuthnRP(
    val name: String,
    val id: String,
)

@Serializable
data class WebAuthnUser(
    val id: String,
    val name: String,
    val displayName: String = name,
)

@Serializable
data class WebAuthnPubKeyCredParams(
    val type: String,
    val alg: Int,
)

@Serializable
data class WebAuthnRegistration(
    val id: String,
    val attestationObject: String,
    val clientDataJSON: String,
)

@Serializable
data class WebAuthnAuthentication(
    val id: String,
    val authenticatorData: String,
    val clientDataJSON: String,
    val signature: String,
    val userHandle: String?
)