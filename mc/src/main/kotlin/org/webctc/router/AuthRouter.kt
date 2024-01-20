package org.webctc.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import net.minecraft.entity.player.EntityPlayer
import org.webctc.WebCTCCore
import org.webctc.common.types.mc.PlayerPrinciple
import org.webctc.plugin.webauthn.authChallenge
import org.webctc.plugin.webauthn.authenticate
import org.webctc.plugin.webauthn.challenge
import org.webctc.plugin.webauthn.register
import java.util.*

class AuthRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit {
        return {
            get("/mc-session-login") {
                call.request.queryParameters["key"]?.let {
                    PlayerSessionManager.useKey(it)
                        ?.let { (name, uuid) -> WebCTCCore.UserSession(name, uuid) }
                        ?.let { session ->
                            call.sessions.set(session)
                            call.respondRedirect("/p/account")
                        }
                }
                call.respondText("Login failed", status = HttpStatusCode.Unauthorized)
            }
            route("/webauthn") {
                authChallenge("/auth-challenge")
                authenticate("/authenticate")
            }

            authenticate("auth-session") {
                get("/profile") {
                    val principal = call.principal<WebCTCCore.UserSession>()!!
                    val profile = PlayerPrinciple(principal.id, principal.uuid.toString())
                    call.respond(profile)
                }

                route("/webauthn") {
                    challenge("/challenge")
                    register("/register")
                }
            }
        }
    }
}

class PlayerSessionManager {
    data class PlayerData(
        val name: String,
        val uuid: UUID
    )

    companion object {
        private val sessionMap = mutableMapOf<String, PlayerData>()

        fun useKey(key: String): PlayerData? {
            return sessionMap.remove(key)
        }

        fun createSession(player: EntityPlayer): String {
            val key = generateKey()
            sessionMap[key] = PlayerData(player.commandSenderName, player.uniqueID)
            return key
        }

        private fun generateKey(): String {
            return (0..15).map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        }
    }
}