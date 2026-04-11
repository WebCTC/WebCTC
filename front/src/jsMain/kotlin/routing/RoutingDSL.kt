package routing

import react.FC
import react.Props
import react.create
import tanstack.react.router.*
import tanstack.router.core.BaseRoute
import tanstack.router.core.RoutePath

data class Routing(
    val router: Router,
) {
    fun createRouterProvider() = RouterProvider.create {
        this.router = this@Routing.router
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Routing) return false
        return router == other.router
    }

    override fun hashCode(): Int = router.hashCode()
}

class RoutingBuilder(
    private val parentRoute: BaseRoute<*>,
) {
    private val routes = mutableListOf<Route>()

    fun page(element: FC<Props>) = page("/", element)

    fun page(path: String, element: FC<Props>) {
        val route = createRoute(
            RouteOptions(
                getParentRoute = { parentRoute },
                path = RoutePath(path),
                component = element
            )
        )

        routes += route
    }

    fun route(path: String, block: RoutingBuilder.() -> Unit) {
        val route = createRoute(
            RouteOptions(
                getParentRoute = { parentRoute },
                path = RoutePath(path),
                component = Outlet
            )
        )

        val builder = RoutingBuilder(route)
        builder.block()

        route.addChildren(builder.build())
        routes += route
    }

    internal fun build(): Array<Route> = routes.toTypedArray()
}

fun routing(
    notFound: FC<Props>? = null,
    block: RoutingBuilder.() -> Unit,
): Routing {
    val rootRoute = createRootRoute(
        RootRouteOptions(
            component = Outlet,
            notFoundComponent = notFound
        )
    )

    val builder = RoutingBuilder(rootRoute)
    builder.block()

    rootRoute.addChildren(builder.build())

    val router = createRouter(
        RouterOptions(
            routeTree = rootRoute
        )
    )

    return Routing(router)
}