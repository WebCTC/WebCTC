package components.tecon.editor

import components.tecon.editor.element.*
import org.webctc.common.types.PosInt2D
import org.webctc.common.types.tecon.shape.*
import react.Props
import react.ReactElement
import react.create

sealed class EditMode(
    val posCount: Int = 0,
    val createIShape: ((List<PosInt2D>) -> IShape?) = { null },
    val createElement: (Props.() -> Unit) -> ReactElement<*>? = { null },
) {
    data object CURSOR : EditMode()
    data object HAND : EditMode()
    data object ERASER : EditMode()
    data object RAIL : EditMode(2, { (start, end) -> RailLine(start, end) }, { RailLineElement.create { it(this) } })
    data object POLYLINE : EditMode(Int.MAX_VALUE, { RailPolyLine(it) }, { RailPolyLineElement.create { it(this) } })
    data object SIGNAL : EditMode(1, { (pos) -> Signal(pos) }, { SignalElement.create { it(this) } })
    data object TECON : EditMode(1, { (pos) -> TeConLever(pos) })
    data object ROUTE : EditMode(1, { (pos) -> Route(pos) })
    data object RECT : EditMode(2, { (start, end) -> RectBox(start, end) }, { RectBoxElement.create { it(this) } })

    companion object {
        private fun findMode(iShape: IShape): EditMode? {
            return when (iShape) {
                is RailLine -> RAIL
                is RailPolyLine -> POLYLINE
                is Signal -> SIGNAL
                is TeConLever -> TECON
                is Route -> ROUTE
                is RectBox -> RECT
                else -> null
            }
        }

        fun createElement(
            iShape: IShape,
            mode: EditMode,
            onSelect: () -> Unit,
            onDelete: () -> Unit,
            selected: Boolean
        ): ReactElement<*>? {
            val baseSetter: (ITeConElementProps) -> Unit = {
                it.mode = mode
                it.onDelete = onDelete
                it.onSelect = onSelect
                it.selected = selected
            }
            val shapeSetter: (IShapeElementProps<IShape>) -> Unit = { it.iShape = iShape }

            val createElement = findMode(iShape)?.createElement ?: return null

            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            return createElement {
                (this as? ITeConElementProps)?.let(baseSetter)
                (this as? IShapeElementProps<IShape>)?.let(shapeSetter)
            }
        }

        fun createPreviewElement(mode: EditMode, posList: List<PosInt2D>): ReactElement<*>? {
            val iShape = mode.createIShape(posList) ?: return null

            val previewProps: (PreviewElementProps) -> Unit = { it.preview = true }
            val shapeSetter: (IShapeElementProps<IShape>) -> Unit = { it.iShape = iShape }

            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            return mode.createElement {
                (this as? PreviewElementProps)?.let(previewProps)
                (this as? IShapeElementProps<IShape>)?.let(shapeSetter)
            }
        }
    }
}