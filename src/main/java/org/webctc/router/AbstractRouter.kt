package org.webctc.router

import io.ktor.server.routing.*

abstract class AbstractRouter {
    abstract fun install(application: Route): Route.() -> Unit
}