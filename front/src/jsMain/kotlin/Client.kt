import js.objects.jso
import pages.MapView
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import web.dom.document

fun main() {
    val container = document.createElement("div").also {
        document.body.appendChild(it)
    }

    val router = createBrowserRouter(arrayOf(jso {
        path = "/"
        element = MapView.create()
    }))

    createRoot(container).render(RouterProvider.create {
        this.router = router
    })
}