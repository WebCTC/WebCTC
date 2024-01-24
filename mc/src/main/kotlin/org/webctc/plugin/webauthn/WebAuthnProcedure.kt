package org.webctc.plugin.webauthn

import com.webauthn4j.WebAuthnManager
import com.webauthn4j.authenticator.AuthenticatorImpl
import com.webauthn4j.data.*
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.util.Base64UrlUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.uuid.UUID
import kotlinx.uuid.fromString
import org.webctc.WebCTCCore
import org.webctc.cache.auth.CredentialData
import org.webctc.common.types.webauthn.*


data class WebAuthnChallenge(val base64Value: String)

fun Route.challenge(path: String) {
    post(path) {
        val challenge = DefaultChallenge()
        val base64Challenge = Base64UrlUtil.encodeToString(challenge.value)


        val headerOrigin = call.request.header(HttpHeaders.Origin) ?: return@post
        val origin = Origin.create(headerOrigin)

        val session = call.principal<WebCTCCore.UserSession>()

        val query = call.request.queryParameters

        val uuid = session?.uuid ?: UUID.fromString(query["uuid"]!!)

        val webAuthnPublicKey = WebAuthnRegistrationOption(
            base64Challenge,
            WebAuthnRP("WebCTC", origin.host!!),
            WebAuthnUser(
                name = session?.id ?: "",
                id = Base64UrlUtil.encodeToString(uuid.toString().toByteArray())
            ),
            listOf(
                WebAuthnPubKeyCredParams("public-key", -7),
                WebAuthnPubKeyCredParams("public-key", -257)
            )
        )

        call.sessions.set(WebAuthnChallenge(base64Challenge))
        call.respond(webAuthnPublicKey)
    }
}

fun Route.authChallenge(path: String) {
    post(path) {
        val challenge = DefaultChallenge()
        val base64Challenge = Base64UrlUtil.encodeToString(challenge.value)

        val headerOrigin = call.request.header(HttpHeaders.Origin) ?: return@post
        val origin = Origin.create(headerOrigin)

        val webAuthnPublicKey = WebAuthnAuthenticationOption(
            base64Challenge,
            origin.host!!,
        )

        call.sessions.set(WebAuthnChallenge(base64Challenge))
        call.respond(webAuthnPublicKey)
    }
}

val webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

fun Route.register(path: String) {
    post(path) {
        try {
            val session = call.principal<WebCTCCore.UserSession>()!!
            val challenge = call.sessions.get<WebAuthnChallenge>()!!
            call.sessions.clear<WebAuthnChallenge>()

            val registration = call.receive<WebAuthnRegistration>()

            val registrationRequest = RegistrationRequest(
                Base64UrlUtil.decode(registration.attestationObject),
                Base64UrlUtil.decode(registration.clientDataJSON)
            )

            val headerOrigin = call.request.header(HttpHeaders.Origin) ?: return@post
            val origin = Origin.create(headerOrigin)

            val registrationParams = RegistrationParameters(
                ServerProperty(
                    origin,
                    origin.host!!,
                    DefaultChallenge(challenge.base64Value),
                    null
                ),
                listOf(
                    PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                    PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256),
                ),
                true
            )

            val registrationData = webAuthnManager.parse(registrationRequest)
            webAuthnManager.validate(registrationData, registrationParams)

            val authenticator = AuthenticatorImpl(
                registrationData.attestationObject!!.authenticatorData.attestedCredentialData!!,
                registrationData.attestationObject!!.attestationStatement,
                registrationData.attestationObject!!.authenticatorData.signCount
            )

            val uuid = UUID(session.uuid.toString())

            CredentialData.registerAuthenticator(uuid, authenticator)
            WebCTCCore.INSTANCE.credentialData.markDirty()

            call.respond(HttpStatusCode.OK)

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}

fun Route.authenticate(path: String) {
    post(path) {
        try {
            val challenge = call.sessions.get<WebAuthnChallenge>()!!
            call.sessions.clear<WebAuthnChallenge>()

            val authentication = call.receive<WebAuthnAuthentication>()

            val decodedUserHandle = authentication.userHandle?.let { Base64UrlUtil.decode(it) }

            val uuid = decodedUserHandle?.joinToString("") { it.toInt().toChar().toString() }

            if (uuid == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val authenticatorContainer = CredentialData.searchCredential(
                UUID(uuid),
                authentication.id.let { Base64UrlUtil.decode(it) }
            )

            if (authenticatorContainer == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val authenticationRequest = AuthenticationRequest(
                authenticatorContainer.authenticator.attestedCredentialData.credentialId,
                decodedUserHandle,
                Base64UrlUtil.decode(authentication.authenticatorData),
                Base64UrlUtil.decode(authentication.clientDataJSON),
                Base64UrlUtil.decode(authentication.signature),
            )

            val headerOrigin = call.request.header(HttpHeaders.Origin) ?: return@post
            val origin = Origin.create(headerOrigin)

            val authenticationParams = AuthenticationParameters(
                ServerProperty(
                    origin,
                    origin.host!!,
                    DefaultChallenge(challenge.base64Value),
                    null
                ),
                authenticatorContainer.authenticator,
                null,
                true
            )

            val authenticationData = webAuthnManager.parse(authenticationRequest)
            webAuthnManager.validate(authenticationData, authenticationParams)

            authenticatorContainer.incrementSignCount()
            WebCTCCore.INSTANCE.credentialData.markDirty()

            call.sessions.set(WebCTCCore.UserSession("", UUID(uuid)))

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}