package org.webctc.router

import com.google.gson.Gson
import com.google.gson.GsonBuilder

abstract class WebCTCRouter : AbstractRouter() {
    protected val gson: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()
}