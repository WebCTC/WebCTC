package org.webctc.router

class RouterManager {
    companion object {
        val routerMap = mutableMapOf<String, AbstractRouter>()

        fun registerRouter(path: String, router: AbstractRouter) {
            routerMap[path] = router
        }
    }
}