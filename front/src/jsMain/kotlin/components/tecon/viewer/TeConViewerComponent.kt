package components.tecon.viewer

import client
import components.tecon.editor.SvgWithDot
import components.tecon.editor.element.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.webctc.common.types.railgroup.RailGroupState
import org.webctc.common.types.signal.SignalState
import org.webctc.common.types.tecon.*
import org.webctc.common.types.tecon.shape.*
import react.FC
import react.Props
import react.useEffect
import react.useState
import utils.useIntervalData
import utils.useListDataWS
import kotlin.time.Duration.Companion.seconds

external interface TeConViewerProps : Props {
    var teConUuid: String
    var parts: List<IShape>
}

val TeConViewer = FC<TeConViewerProps> { props ->
    val parts = props.parts

    val rgStateList by useListDataWS<RailGroupState>(
        "/api/railgroups/state/ws",
        (parts.filterIsInstance<RailShape>().flatMap { it.railGroupList } +
                parts.filterIsInstance<TrainNumber>().flatMap { it.railGroupList }).toSet()
    ) { a, b -> a.uuid == b.uuid }

    val signalStateList by useListDataWS<SignalState>(
        "/api/signals/state/ws",
        parts.filterIsInstance<Signal>().map { it.signalPos }.toSet()
    ) { a, b -> a.pos == b.pos }

    val polledRuntime by useIntervalData<TeConRuntimeState>("/api/tecons/${props.teConUuid}/runtime", 1.seconds)
    var localRuntime by useState<TeConRuntimeState>()
    var armedLeverId by useState<String?>(null)
    var armedSide by useState<TeConLeverSide?>(null)

    useEffect(polledRuntime) {
        polledRuntime?.let { localRuntime = it }
    }

    val runtime = localRuntime
    val leverRuntimeById = runtime?.leverStates?.associateBy { it.leverId }.orEmpty()

    fun clearArmed() {
        armedLeverId = null
        armedSide = null
    }

    fun operate(request: TeConOperateRequest, onDone: () -> Unit = {}) {
        MainScope().launch {
            runCatching {
                val response = client.post("/api/tecons/${props.teConUuid}/operate") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                val payload = runCatching { response.body<TeConOperateResponse>() }.getOrNull()
                payload ?: TeConOperateResponse(false, response.bodyAsText())
            }.onSuccess {
                localRuntime = it.runtimeState
                if (it.ok) {
                    onDone()
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun onLeverOperate(lever: TeConLever, side: TeConLeverSide) {
        val runtimeState = leverRuntimeById[lever.id]
        if (runtimeState?.activeSide == side) {
            operate(
                TeConOperateRequest(
                    leverId = lever.id,
                    side = side,
                    routeId = runtimeState.routeId,
                    actionType = TeConOperateActionType.RETURN
                ),
                ::clearArmed
            )
            return
        }

        if (armedLeverId == lever.id && armedSide == side) {
            clearArmed()
            return
        }

        when (val sideConfig = if (side == TeConLeverSide.L) lever.left else lever.right) {
            is DirectLeverSide -> {
                operate(
                    TeConOperateRequest(
                        leverId = lever.id,
                        side = side,
                        actionType = TeConOperateActionType.REQUEST
                    ),
                    ::clearArmed
                )
            }

            is SelectLeverSide -> {
                if (sideConfig.actions.isNotEmpty()) {
                    armedLeverId = lever.id
                    armedSide = side
                }
            }

            is DisabledLeverSide -> Unit
        }
    }

    fun onRouteOperate(route: Route) {
        val lever = parts.filterIsInstance<TeConLever>().find { it.id == armedLeverId } ?: return
        val side = armedSide ?: return
        val sideConfig = if (side == TeConLeverSide.L) lever.left else lever.right
        if (sideConfig is SelectLeverSide && sideConfig.actions.any { it.routeId == route.id }) {
            operate(
                TeConOperateRequest(
                    leverId = lever.id,
                    side = side,
                    routeId = route.id,
                    actionType = TeConOperateActionType.REQUEST
                ),
                ::clearArmed
            )
        }
    }

    SvgWithDot {
        dotVisibility = false
        cursorVisibility = false

        parts.forEach { shape ->
            when (shape) {
                is RailLine -> RailLineElement {
                    iShape = shape
                    rgState = rgStateList.filter { rg -> rg.uuid in shape.railGroupList }.toSet()
                }

                is RailPolyLine -> RailPolyLineElement {
                    iShape = shape
                    rgState = rgStateList.filter { rg -> rg.uuid in shape.railGroupList }.toSet()
                }

                is Signal -> SignalElement {
                    iShape = shape
                    signalState = signalStateList.find { ss -> shape.signalPos == ss.pos }
                }

                is RectBox -> RectBoxElement {
                    iShape = shape
                }

                is FreeText -> FreeTextElement {
                    iShape = shape
                }

                is TrainNumber -> TrainNumberElement {
                    iShape = shape
                    trainCustomName =
                        rgStateList.firstOrNull { rg -> rg.uuid in shape.railGroupList && !rg.trainName.isNullOrEmpty() }?.trainName
                }

                is TeConLever -> TeConLeverElement {
                    iShape = shape
                    runtimeState = leverRuntimeById[shape.id]
                    this.armedSide = armedSide.takeIf { armedLeverId == shape.id }
                    onOperate = { side -> onLeverOperate(shape, side) }
                }

                is Route -> RouteElement {
                    iShape = shape
                    val armedLever = parts.filterIsInstance<TeConLever>().find { it.id == armedLeverId }
                    val armedConfig = when (armedSide) {
                        TeConLeverSide.L -> armedLever?.left
                        TeConLeverSide.R -> armedLever?.right
                        null -> null
                    }
                    enabled = armedConfig is SelectLeverSide && armedConfig.actions.any { it.routeId == shape.id }
                    active = runtime?.leverStates?.any { it.routeId == shape.id } == true
                    onTrigger = { onRouteOperate(shape) }
                }
            }
        }
    }
}
