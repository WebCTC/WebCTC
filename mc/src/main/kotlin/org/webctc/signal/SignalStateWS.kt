package org.webctc.signal

import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.webctc.cache.signal.SignalCacheData
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.signal.SignalState
import java.util.concurrent.CopyOnWriteArrayList

data class SignalStateWS(
    val signal: SignalData,
    val connection: WebSocketServerSession,
    var lastState: SignalState? = null,
) {
    init {
        signalStateWSMap.add(this)
    }

    fun trySendState() {
        val state = signal.getState()
        if (state == lastState) return
        lastState = state
        MainScope().launch(Dispatchers.IO) { connection.sendSerialized(state) }
    }

    fun close() {
        signalStateWSMap.remove(this)
    }

    companion object {
        val signalStateWSMap = CopyOnWriteArrayList<SignalStateWS>()

        fun sendAll() {
            signalStateWSMap.forEach { it.trySendState() }
        }
    }
}

fun SignalData.getState(): SignalState {
    val signalLevel = SignalCacheData.signalMapCache[this.pos]?.signalLevel ?: 0
    return SignalState(signalLevel)
}
