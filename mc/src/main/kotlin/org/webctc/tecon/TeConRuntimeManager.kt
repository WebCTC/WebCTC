package org.webctc.tecon

import net.minecraft.init.Blocks
import net.minecraft.server.MinecraftServer
import org.webctc.cache.tecon.TeConData
import org.webctc.common.types.PosInt
import org.webctc.common.types.tecon.*
import org.webctc.common.types.tecon.operation.ITeConOperation
import org.webctc.common.types.tecon.operation.JavaScriptOperation
import org.webctc.common.types.tecon.operation.RedStoneOperation
import org.webctc.common.types.tecon.operation.ReserveOperation
import org.webctc.common.types.tecon.shape.*
import org.webctc.railgroup.RailGroupData
import java.util.concurrent.ConcurrentHashMap
import javax.script.ScriptEngineManager
import kotlin.uuid.Uuid

object TeConRuntimeManager {
    private data class ActiveTeConAction(
        val teConUuid: Uuid,
        val leverId: String,
        val side: TeConLeverSide,
        val routeId: String?,
        val actionName: String,
    )

    private val activeActionMap = ConcurrentHashMap<Uuid, ConcurrentHashMap<String, ActiveTeConAction>>()
    private val scriptEngineManager = ScriptEngineManager()

    fun getRuntimeState(teCon: TeCon): TeConRuntimeState {
        val leverStates = activeActionMap[teCon.uuid]
            ?.values
            ?.map { active ->
                TeConLeverRuntimeState(
                    leverId = active.leverId,
                    activeSide = active.side,
                    routeId = active.routeId,
                    actionName = active.actionName,
                    cancelable = teCon.findLever(active.leverId)
                        ?.findAction(active.side, active.routeId)
                        ?.let { canCancel(it) }
                        ?: false
                )
            }
            ?.sortedBy { it.leverId }
            ?: emptyList()
        return TeConRuntimeState(leverStates)
    }

    fun operate(teCon: TeCon, request: TeConOperateRequest, session: Any?): TeConOperateResponse {
        val lever = teCon.findLever(request.leverId)
            ?: return TeConOperateResponse(false, "Lever not found", getRuntimeState(teCon))
        val sideConfig = lever.getSideConfig(request.side)
        val activeMap = activeActionMap.getOrPut(teCon.uuid) { ConcurrentHashMap() }
        val activeAction = activeMap[lever.id]

        return when (request.actionType) {
            TeConOperateActionType.REQUEST -> handleRequest(teCon, lever, sideConfig, request, activeAction, session)
            TeConOperateActionType.RETURN -> handleReturn(teCon, lever, request, activeAction, session)
        }
    }

    fun tick() {
        val teConList = TeConData.teConList
        activeActionMap.entries.removeIf { (teConUuid, leverMap) ->
            val teCon = teConList[teConUuid]
            if (teCon == null) {
                leverMap.values.forEach { deactivateRedStone(it, teConList[teConUuid]) }
                true
            } else {
                leverMap.entries.removeIf { (_, active) ->
                    val action = teCon.findLever(active.leverId)?.findAction(active.side, active.routeId)
                    if (action == null || !isActionStillActive(teCon, active, action)) {
                        deactivateRedStone(active, teCon)
                        true
                    } else {
                        false
                    }
                }
                leverMap.isEmpty()
            }
        }
    }

    private fun handleRequest(
        teCon: TeCon,
        lever: TeConLever,
        sideConfig: TeConLeverSideConfig,
        request: TeConOperateRequest,
        activeAction: ActiveTeConAction?,
        session: Any?,
    ): TeConOperateResponse {
        if (activeAction != null) {
            return TeConOperateResponse(false, "Lever already active", getRuntimeState(teCon))
        }

        val action = when (sideConfig) {
            is DirectLeverSide -> sideConfig.action.takeIf { request.routeId == null }
            is SelectLeverSide -> sideConfig.actions.find { it.routeId == request.routeId }
            is DisabledLeverSide -> null
            else -> null
        } ?: return TeConOperateResponse(false, "Action not found", getRuntimeState(teCon))

        val route = request.routeId?.let(teCon::findRoute)
        val performed = mutableListOf<ITeConOperation>()

        try {
            action.operations.forEach { operation ->
                if (!executeOperation(
                        teCon,
                        lever,
                        request.side,
                        route,
                        action,
                        operation,
                        request.actionType,
                        session
                    )
                ) {
                    rollback(teCon, lever, request.side, route, action, performed, session)
                    return TeConOperateResponse(false, "Operation failed", getRuntimeState(teCon))
                }
                performed += operation
            }
        } catch (_: Throwable) {
            rollback(teCon, lever, request.side, route, action, performed, session)
            return TeConOperateResponse(false, "Operation failed", getRuntimeState(teCon))
        }

        activeActionMap.getOrPut(teCon.uuid) { ConcurrentHashMap() }[lever.id] = ActiveTeConAction(
            teConUuid = teCon.uuid,
            leverId = lever.id,
            side = request.side,
            routeId = action.routeId,
            actionName = action.name,
        )
        return TeConOperateResponse(true, "OK", getRuntimeState(teCon))
    }

    private fun handleReturn(
        teCon: TeCon,
        lever: TeConLever,
        request: TeConOperateRequest,
        activeAction: ActiveTeConAction?,
        session: Any?,
    ): TeConOperateResponse {
        if (activeAction == null || activeAction.side != request.side) {
            return TeConOperateResponse(false, "Active side not found", getRuntimeState(teCon))
        }

        val action = lever.findAction(activeAction.side, activeAction.routeId)
            ?: return TeConOperateResponse(false, "Action not found", getRuntimeState(teCon))

        if (!canCancel(action)) {
            return TeConOperateResponse(false, "Action is locked", getRuntimeState(teCon))
        }

        val route = activeAction.routeId?.let(teCon::findRoute)

        try {
            action.operations.reversed().forEach { operation ->
                executeReverseOperation(
                    teCon,
                    lever,
                    activeAction.side,
                    route,
                    action,
                    operation,
                    session
                )
            }
        } catch (_: Throwable) {
            return TeConOperateResponse(false, "Return failed", getRuntimeState(teCon))
        }

        activeActionMap[teCon.uuid]?.remove(lever.id)
        if (activeActionMap[teCon.uuid]?.isEmpty() == true) {
            activeActionMap.remove(teCon.uuid)
        }
        return TeConOperateResponse(true, "OK", getRuntimeState(teCon))
    }

    private fun rollback(
        teCon: TeCon,
        lever: TeConLever,
        side: TeConLeverSide,
        route: Route?,
        action: TeConAction,
        performed: List<ITeConOperation>,
        session: Any?,
    ) {
        performed.asReversed().forEach {
            runCatching {
                executeReverseOperation(teCon, lever, side, route, action, it, session)
            }
        }
    }

    private fun executeOperation(
        teCon: TeCon,
        lever: TeConLever,
        side: TeConLeverSide,
        route: Route?,
        action: TeConAction,
        operation: ITeConOperation,
        actionType: TeConOperateActionType,
        session: Any?,
    ): Boolean {
        return when (operation) {
            is ReserveOperation -> {
                val uuids = operation.chain.chain.toTypedArray()
                RailGroupData.reserve(uuids, operation.resolveKey(teCon, lever, side, action))
            }

            is RedStoneOperation -> {
                setRedStone(operation.redStonePosSet, true)
                true
            }

            is JavaScriptOperation -> executeJavaScript(
                teCon,
                lever,
                side,
                route,
                action,
                actionType,
                operation,
                session
            )

            else -> false
        }
    }

    private fun executeReverseOperation(
        teCon: TeCon,
        lever: TeConLever,
        side: TeConLeverSide,
        route: Route?,
        action: TeConAction,
        operation: ITeConOperation,
        session: Any?,
    ) {
        when (operation) {
            is ReserveOperation -> RailGroupData.release(
                operation.chain.chain.toTypedArray(),
                operation.resolveKey(teCon, lever, side, action)
            )

            is RedStoneOperation -> setRedStone(operation.redStonePosSet, false)
            is JavaScriptOperation -> {
                val ok = executeJavaScript(
                    teCon,
                    lever,
                    side,
                    route,
                    action,
                    TeConOperateActionType.RETURN,
                    operation,
                    session
                )
                if (!ok) {
                    throw IllegalStateException("JavaScript RETURN failed")
                }
            }

            else -> Unit
        }
    }

    private fun executeJavaScript(
        teCon: TeCon,
        lever: TeConLever,
        side: TeConLeverSide,
        route: Route?,
        action: TeConAction,
        actionType: TeConOperateActionType,
        operation: JavaScriptOperation,
        session: Any?,
    ): Boolean {
//        val engine = scriptEngineManager.getEngineByName("nashorn") ?: return false
//        engine.put("tecon", teCon)
//        engine.put("lever", lever)
//        engine.put("side", side.name)
//        engine.put("route", route)
//        engine.put("action", action)
//        engine.put("actionType", actionType.name)
//        engine.put("session", session)
//        engine.put("RailGroupManager", org.webctc.webctcex.utils.RailGroupManager())
//        val result = engine.eval(operation.script)
//        return result as? Boolean ?: false
        return true
    }

    private fun setRedStone(posSet: Set<PosInt>, active: Boolean) {
        val world = MinecraftServer.getServer().entityWorld
        val block = if (active) Blocks.redstone_block else Blocks.stained_glass
        posSet.forEach {
            world.setBlock(it.x, it.y, it.z, block, 14, 3)
        }
    }

    private fun canCancel(action: TeConAction): Boolean {
        val reserveOperations = action.operations.filterIsInstance<ReserveOperation>()
        return reserveOperations.all { reserve ->
            reserve.chain.chain.all { !RailGroupData.isTrainOnRail(it) }
        }
    }

    private fun isActionStillActive(teCon: TeCon, active: ActiveTeConAction, action: TeConAction): Boolean {
        val lever = teCon.findLever(active.leverId) ?: return false
        val reserveOperations = action.operations.filterIsInstance<ReserveOperation>()
        if (reserveOperations.isEmpty()) {
            return true
        }
        return reserveOperations.all { reserve ->
            val uuids = reserve.chain.chain.toTypedArray()
            val key = reserve.resolveKey(teCon, lever, active.side, action)
            RailGroupData.isLocked(uuids, key) || RailGroupData.isReserved(uuids, key)
        }
    }

    private fun deactivateRedStone(active: ActiveTeConAction, teCon: TeCon?) {
        val action = teCon?.findLever(active.leverId)?.findAction(active.side, active.routeId) ?: return
        action.operations.filterIsInstance<RedStoneOperation>().forEach { setRedStone(it.redStonePosSet, false) }
    }
}

private fun TeCon.findLever(leverId: String): TeConLever? {
    return parts.filterIsInstance<TeConLever>().find { it.id == leverId }
}

private fun TeCon.findRoute(routeId: String): Route? {
    return parts.filterIsInstance<Route>().find { it.id == routeId }
}

private fun TeCon.operationKey(leverId: String, side: TeConLeverSide, routeId: String?): String {
    return listOf(uuid.toString(), leverId, side.name, routeId.orEmpty()).joinToString(":")
}

private fun ReserveOperation.resolveKey(
    teCon: TeCon,
    lever: TeConLever,
    side: TeConLeverSide,
    action: TeConAction,
): String {
    return key.ifBlank { teCon.operationKey(lever.id, side, action.routeId) }
}

private fun TeConLever.getSideConfig(side: TeConLeverSide): TeConLeverSideConfig {
    return if (side == TeConLeverSide.L) left else right
}

private fun TeConLever.findAction(side: TeConLeverSide, routeId: String?): TeConAction? {
    return when (val config = getSideConfig(side)) {
        is DirectLeverSide -> config.action.takeIf { routeId == null }
        is SelectLeverSide -> config.actions.find { it.routeId == routeId }
        is DisabledLeverSide -> null
        else -> null
    }
}
