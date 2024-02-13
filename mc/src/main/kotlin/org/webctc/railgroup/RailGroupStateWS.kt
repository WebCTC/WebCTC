package org.webctc.railgroup

import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupState
import java.util.concurrent.CopyOnWriteArrayList

data class RailGroupStateWS(
    val railGroup: RailGroup,
    val connection: WebSocketServerSession,
    var lastState: RailGroupState? = null,
) {
    init {
        railGroupStateWSMap.add(this)
    }

    fun trySendState() {
        val state = railGroup.getState()
        if (state == lastState) return
        lastState = state
        MainScope().launch(Dispatchers.IO) { connection.sendSerialized(state) }
    }

    fun close() {
        railGroupStateWSMap.remove(this)
    }

    companion object {
        val railGroupStateWSMap = CopyOnWriteArrayList<RailGroupStateWS>()

        fun sendAll() {
            railGroupStateWSMap.forEach { it.trySendState() }
        }
    }
}
