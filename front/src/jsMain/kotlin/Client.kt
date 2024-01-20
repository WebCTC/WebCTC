import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import js.objects.jso
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.webctc.common.types.rail.IRailMapData
import org.webctc.common.types.rail.RailMapData
import org.webctc.common.types.rail.RailMapSwitchData
import pages.Account
import pages.FallBack
import pages.Login
import pages.MapView
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import web.dom.document

val client = HttpClient(Js) {
    val jsonPreset = Json {
        serializersModule = SerializersModule {
            polymorphic(IRailMapData::class) {
                subclass(RailMapData::class)
                subclass(RailMapSwitchData::class)
            }
        }
        ignoreUnknownKeys = true
    }

    install(ContentNegotiation) {
        json(jsonPreset)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(jsonPreset)
    }
}

fun main() {
    val container = document.createElement("div").also {
        document.body.appendChild(it)

        document.body.style.apply {
            fontFamily = "'Noto Sans JP', sans-serif"
        }
    }

    val router = createBrowserRouter(arrayOf(
        jso {
            path = "/"
            element = MapView.create()
        }, jso {
            path = "/p/account"
            element = Account.create()
        }, jso {
            path = "/login"
            element = Login.create()
        }, jso {
            path = "*"
            element = FallBack.create()
        }
    ))

    createRoot(container).render(RouterProvider.create {
        this.router = router
    })
}