package org.webctc.router.api

import express.ExpressRouter

class ApiRouter : ExpressRouter() {
    init {
        get("/") { req, res ->
            res.send(
                "This is WebCTC API. \n" +
                        "http://${req.host}${req.uri}\n" +
                        "http://${req.host}${req.uri}formations/\n" +
                        "http://${req.host}${req.uri}formations/<formationId>\n" +
                        "http://${req.host}${req.uri}formations/<formationId>/trains\n" +
                        "http://${req.host}${req.uri}trains\n" +
                        "http://${req.host}${req.uri}trains/<entityId>\n"
            )
        }
    }
}