package org.webctc.router

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import express.ExpressRouter

open class WebCTCRouter : ExpressRouter() {
    protected val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()
}