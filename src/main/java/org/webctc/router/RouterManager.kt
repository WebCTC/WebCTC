package org.webctc.router

import express.ExpressRouter

class RouterManager {
    companion object {
        val routerMap = mutableMapOf<String, ExpressRouter>()

        fun registerRouter(path: String, router: ExpressRouter) {
            routerMap[path] = router
        }
    }
}