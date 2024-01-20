package pages

import client
import components.Header
import components.SkeletonSpan
import emotion.react.css
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import js.buffer.ArrayBuffer
import js.objects.jso
import js.promise.await
import js.typedarrays.Uint8Array
import js.typedarrays.toUint8Array
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.Add
import mui.material.*
import mui.system.Breakpoint
import org.webctc.common.types.mc.PlayerPrinciple
import org.webctc.common.types.mc.PlayerProfile
import org.webctc.common.types.webauthn.WebAuthnRegistration
import org.webctc.common.types.webauthn.WebAuthnRegistrationOption
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.p
import web.authn.AuthenticatorAttestationResponse
import web.authn.PublicKeyCredential
import web.authn.PublicKeyCredentialParameters
import web.authn.PublicKeyCredentialType
import web.credentials.CredentialCreationOptions
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.rem
import web.navigator.navigator
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val Account = FC<Props> {
    var playerUuid by useState<String>()
    var playerName by useState<String>()

    var passkeyResult by useState<String>()
    var passkeyDialogOpen by useState(false)

    useEffectOnce {
        MainScope().launch {
            val playerPrinciple: PlayerPrinciple = client.get("/auth/profile").body()
            val uuid = playerPrinciple.uuid
            playerUuid = uuid
            playerName = playerPrinciple.name.ifEmpty {
                val playerProfile: PlayerProfile = client.get("https://mc-heads.net/minecraft/profile/$uuid").body()
                playerProfile.name
            }
        }
    }

    CssBaseline {}
    Header {}
    Container {
        maxWidth = Breakpoint.sm
        h1 { +"アカウント情報" }

        div {
            css {
                display = Display.flex
                alignItems = AlignItems.center
            }

            div {
                css {
                    paddingRight = 1.5.rem
                }
                if (playerUuid.isNullOrEmpty()) {
                    Skeleton {
                        variant = SkeletonVariant.circular
                        width = 5.rem
                        height = 5.rem
                    }
                } else {
                    img {
                        src = "https://mc-heads.net/avatar/$playerUuid"
                        style = jso {
                            width = 5.rem
                            height = 5.rem
                        }
                    }
                }
            }

            div {
                p {
                    SkeletonSpan {
                        prefix = "MCID"
                        width = 10.rem
                        text = playerName
                    }
                }
                p {
                    SkeletonSpan {
                        prefix = "UUID"
                        width = 10.rem
                        text = playerUuid
                    }
                }
            }

        }

        h1 { +"Passkey" }

        Box {
            p { +"WebCTCはPasskeyでのログインに対応しています。(※SSL接続の場合)" }
            p { +"Passkeyを利用することで、MinecraftサーバーでコマンドからセッションURLを発行することなくログインできます。" }
        }


        Button {
            +"Passkeyを追加"
            variant = ButtonVariant.contained
            color = ButtonColor.primary
            startIcon = Add.create()
            onClick = {
                passkeyDialogOpen = true
                passkeyResult = "Passkeyの登録中..."
                MainScope().launch {
                    try {
                        val pubKey: WebAuthnRegistrationOption = client.post("/auth/webauthn/challenge").body()
                        val credential = navigator.credentials.create(pubKey.toOption()).await() as? PublicKeyCredential
                            ?: throw Exception("Credential is null")

                        val credentialData = credential.response as AuthenticatorAttestationResponse
                        val clientDataJSON = credentialData.clientDataJSON
                        val attestationObject = credentialData.attestationObject
                        val id = credential.id

                        val passKey = WebAuthnRegistration(
                            id, attestationObject.toBase64Url(), clientDataJSON.toBase64Url(),
                        )
                        client.post("/auth/webauthn/register") {
                            contentType(ContentType.Application.Json)
                            setBody(passKey)
                        }.let {
                            if (it.status == HttpStatusCode.OK) {
                                passkeyResult =
                                    "Passkeyの登録が完了しました。今後は、MinecraftサーバーでコマンドからセッションURLを発行することなく、Passkeyを利用してログインできます。"
                                return@launch
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    passkeyResult = "Passkeyの登録に失敗しました。"
                }
            }
        }

        Dialog {
            onClose = { _, _ -> passkeyDialogOpen = false }
            fullWidth = true
            maxWidth = Breakpoint.sm
            open = passkeyDialogOpen
            DialogTitle { +"Passkey登録" }
            DialogContent { DialogContentText { +passkeyResult } }
        }
    }
}

fun WebAuthnRegistrationOption.toOption(): CredentialCreationOptions {
    val kotlinData = this

    return jso {
        publicKey = jso {
            timeout = kotlinData.timeout
            challenge = kotlinData.challenge.toBuffer()
            rp = jso {
                id = kotlinData.rp.id
                name = kotlinData.rp.name
            }
            user = jso {
                id = kotlinData.user.id.toBuffer()
                name = kotlinData.user.name
                displayName = kotlinData.user.displayName
            }
            pubKeyCredParams = kotlinData.pubKeyCredParams.map {
                jso<PublicKeyCredentialParameters> {
                    type = PublicKeyCredentialType.publicKey
                    alg = it.alg
                }
            }.toTypedArray()
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun String.toBuffer(): ArrayBuffer {
    val byteArray = Base64.UrlSafe.decode(this)

    return byteArray.toUint8Array().buffer.unsafeCast<ArrayBuffer>()
}

fun ArrayBuffer.toByteArray(): ByteArray = Uint8Array(this).toByteArray()

@OptIn(ExperimentalEncodingApi::class)
fun ArrayBuffer.toBase64Url(): String {
    val byteArray = this.toByteArray()

    return Base64.UrlSafe.encode(byteArray)
}