package components.tecon.editor

import org.webctc.common.types.PosInt2D
import org.webctc.common.types.tecon.shape.*

enum class EditMode(
    val posCount: Int = 0,
    val create: ((List<PosInt2D>) -> IShape?) = { null }
) {
    CURSOR,
    HAND,
    ERASER,
    RAIL(2, { (start, end) -> RailLine(start, end) }),
    POLYLINE(Int.MAX_VALUE, { RailPolyLine(it) }),
    SIGNAL(1, { (pos) -> Signal(pos) }),
    TECON(1, { (pos) -> TeConLever(pos) }),
    ROUTE(1, { (pos) -> Route(pos) });
}