import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import org.webctc.common.types.kotlinxJson
import pages.*
import pages.tecon.TeConEdit
import pages.tecon.TeConList
import pages.tecon.TeConView
import react.dom.client.createRoot
import routing.routing
import web.dom.document

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json(kotlinxJson)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(kotlinxJson)
    }
}

fun main() {
    val root = document.createElement("div")
        .apply { this.style.fontFamily = "'Noto Sans JP', sans-serif" }
        .apply { document.body.appendChild(this) }
        .let(::createRoot)

    val routing = routing(notFound = FallBack) {
        page(MapView)
        page("login", Login)

        route("p") {
            page("account", Account)
            page("railgroup", RailGroupManager)
            page("waypoint", WayPointEditor)

            route("tecons") {
                page(TeConList)
                page("view/:uuid", TeConView)
                page("edit/:uuid", TeConEdit)
            }
        }
    }

    val provider = routing.createRouterProvider()
    root.render(provider)
}