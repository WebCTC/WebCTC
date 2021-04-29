package org.webctc.router

import express.ExpressRouter

class DefaultRouter : ExpressRouter() {
    init {
        get("/") { req, res ->
            res.send("Hello World!")
        }
    }
}