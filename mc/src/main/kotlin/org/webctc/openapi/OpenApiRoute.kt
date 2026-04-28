package org.webctc.openapi

import kotlin.reflect.KClass

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class OpenApiRoute(
    val summary: String = "",
    val request: KClass<*> = Unit::class,
    val response: KClass<*> = Unit::class,
    val responseList: Boolean = false,
    val query: String = "",
    val authenticated: Boolean = false,
    val docPath: String = "",
)
