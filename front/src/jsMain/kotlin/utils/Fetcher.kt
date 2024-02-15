package utils

import client
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.StateInstance
import react.useEffect
import react.useState
import web.timers.clearInterval
import web.timers.setInterval
import kotlin.time.Duration

inline fun <reified R : Any> useData(url: String?, notFound: () -> Unit = {}): StateInstance<R?> {
    val stateInstance = useState<R>()
    val (data, setData) = stateInstance
    useEffect(url) {
        if (url.isNullOrEmpty()) {
            setData { null }
            return@useEffect
        }
        var ignore = false
        MainScope().launch {
            val res: R = client.get(url).body()
            if (ignore) return@launch
            setData { res }
        }
        cleanup {
            ignore = true
        }
    }
    return stateInstance
}

inline fun <reified R : Any> useIntervalData(url: String?, interval: Duration): StateInstance<R?> {
    val stateInstance = useState<R>()
    val (data, setData) = stateInstance
    useEffect(url) {
        if (url.isNullOrEmpty()) {
            setData { null }
            return@useEffect
        }
        var ignore = false
        val intervalId = setInterval(interval) {
            MainScope().launch {
                val res: R = client.get(url).body()
                if (ignore) return@launch
                setData { res }
            }
        }
        cleanup {
            ignore = true
            clearInterval(intervalId)
        }
    }
    return stateInstance
}

inline fun <reified R : Any> useDataWithWebsocket(
    path: String,
    wsPath: String,
    crossinline equals: (R?, R?) -> Boolean
): StateInstance<R?> {
    val stateInstance = useState<R>()
    val (data, setData) = stateInstance

    val protocol = window.location.protocol
    val wsProtocol = if (protocol == "https:") URLProtocol.WSS else URLProtocol.WS
    val port = window.location.port.toIntOrNull() ?: wsProtocol.defaultPort

    useEffect(path) {
        var ignore = false
        MainScope().launch {
            val res: R = client.get(path).body()
            if (ignore) return@launch
            setData { res }
        }
        MainScope().launch {
            client.ws(wsPath, {
                url.protocol = wsProtocol
                url.port = port
            }) {
                while (!ignore) {
                    val received = receiveDeserialized<R>()
                    setData { if (equals(it, received)) it else received }
                }
            }
        }
        cleanup {
            ignore = true
        }
    }
    return stateInstance
}

inline fun <reified R : Any> useListDataWS(
    wsPath: String,
    bodyData: Any,
    crossinline equals: (R, R) -> Boolean
): StateInstance<List<R>> {
    val stateInstance = useState<List<R>>(listOf())
    val (data, setData) = stateInstance

    val protocol = window.location.protocol
    val wsProtocol = if (protocol == "https:") URLProtocol.WSS else URLProtocol.WS
    val port = window.location.port.toIntOrNull() ?: wsProtocol.defaultPort

    useEffect(wsPath) {
        var ignore = false
        MainScope().launch {
            client.ws(wsPath, {
                url.protocol = wsProtocol
                url.port = port
            }) {
                sendSerialized(bodyData)
                while (!ignore) {
                    val received = receiveDeserialized<R>()
                    setData {
                        it.filterNot { old -> equals(old, received) } + received
                    }
                }
            }
        }
        cleanup { ignore = true }
    }
    return stateInstance
}

inline fun <reified R : Any> useListData(url: String?): StateInstance<List<R>> {
    val stateInstance = useState<List<R>>(listOf())
    val (data, setData) = stateInstance
    useEffect(url) {
        var ignore = false
        if (url.isNullOrEmpty()) {
            setData { listOf() }
            return@useEffect
        }
        MainScope().launch {
            val res: List<R> = client.get(url).body()
            if (ignore) return@launch
            setData { res }
        }
        cleanup {
            ignore = true
        }
    }
    return stateInstance
}

inline fun <reified R : Any> useIntervalListData(url: String?, interval: Duration): StateInstance<List<R>> {
    val stateInstance = useState<List<R>>(listOf())
    val (data, setData) = stateInstance

    useEffect(url) {
        var ignore = false
        if (url.isNullOrEmpty()) {
            setData { listOf() }
            return@useEffect
        }
        val intervalId = setInterval(interval) {
            MainScope().launch {
                val res: List<R> = client.get(url).body()
                if (ignore) return@launch
                setData { res }
            }
        }
        cleanup {
            ignore = true
            clearInterval(intervalId)
        }
    }
    return stateInstance
}

inline fun <reified R : Any> useListDataWithWebsocket(
    path: String,
    wsPath: String,
    crossinline equals: (R, R) -> Boolean
): StateInstance<List<R>> {
    val stateInstance = useState<List<R>>(listOf())
    val (data, setData) = stateInstance

    val protocol = window.location.protocol
    val wsProtocol = if (protocol == "https:") URLProtocol.WSS else URLProtocol.WS
    val port = window.location.port.toIntOrNull() ?: wsProtocol.defaultPort

    useEffect(path) {
        var ignore = false
        MainScope().launch {
            val res: List<R> = client.get(path).body()
            if (ignore) return@launch
            setData { res }
        }
        MainScope().launch {
            client.ws(wsPath, {
                url.protocol = wsProtocol
                url.port = port
            }) {
                while (!ignore) {
                    val receivedList = receiveDeserialized<List<R>>()
                    setData {
                        it.filterNot { old -> receivedList.any { new -> equals(old, new) } } + receivedList
                    }
                }
            }
        }
        cleanup {
            ignore = true
        }
    }
    return stateInstance
}