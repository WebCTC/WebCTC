import js.objects.jso
import pages.FallBack
import pages.MapView
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import web.dom.document

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
            path = "*"
            element = FallBack.create()
        })
    )

    createRoot(container).render(RouterProvider.create {
        this.router = router
    })
}