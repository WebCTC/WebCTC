package components.tecon.editor

import components.tecon.editor.element.*
import org.webctc.common.types.PosInt2D
import org.webctc.common.types.tecon.shape.*
import react.Props
import react.ReactElement
import react.create

sealed class EditMode(
    val name: String,
    val key: Char,
    val posCount: Int = 0,
    val createIShape: ((List<PosInt2D>) -> IShape?) = { null },
    val viewElement: (Props.() -> Unit) -> ReactElement<*>? = { null },
    val propertyElement: (Props.() -> Unit) -> ReactElement<*>? = { null }
) {
    data object CURSOR : EditMode("Select", 'V')
    data object HAND : EditMode("Hand", 'H')
    data object ERASER : EditMode("Eraser", 'E')

    data object RAIL : EditMode(
        "Rail", 'R',
        2, { (start, end) -> RailLine(start, PosInt2D.ZERO, end - start) },
        { RailLineElement.create { it(this) } },
        { RailLineProperty.create { it(this) } }
    )

    data object POLYLINE : EditMode(
        "PolyLine Rail", 'P',
        Int.MAX_VALUE, {
            val first = it.first()
            RailPolyLine(first, it.map { pos -> pos - first })
        },
        { RailPolyLineElement.create { it(this) } },
        { RailPolyLineProperty.create { it(this) } }
    )

    data object SIGNAL : EditMode(
        "Signal", 'S',
        1, { (pos) -> Signal(pos) },
        { SignalElement.create { it(this) } },
        { SignalProperty.create { it(this) } }
    )

    data object TECON : EditMode(
        "TeCon Lever", 'L',
        1, { (pos) -> TeConLever(pos) })

    data object FREETEXT : EditMode(
        "Free Text", 'T',
        1, { (pos) -> FreeText(pos) },
        { FreeTextElement.create { it(this) } },
        { FreeTextProperty.create { it(this) } }
    )

    data object ROUTE : EditMode(
        "TeCon Route", ';',
        1, { (pos) -> Route(pos) })

    data object RECT : EditMode(
        "Station", 'T',
        2, { (start, end) -> RectBox(start, PosInt2D.ZERO, end - start) },
        { RectBoxElement.create { it(this) } })

    fun isInfinitySelection() = posCount == Int.MAX_VALUE

    companion object {
        private fun findMode(iShape: IShape): EditMode? {
            return when (iShape) {
                is RailLine -> RAIL
                is RailPolyLine -> POLYLINE
                is Signal -> SIGNAL
                is TeConLever -> TECON
                is FreeText -> FREETEXT
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

            val iShapeMode = findMode(iShape) ?: return null
            val createElement = iShapeMode.viewElement

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
            return mode.viewElement {
                (this as? PreviewElementProps)?.let(previewProps)
                (this as? IShapeElementProps<IShape>)?.let(shapeSetter)
            }
        }

        fun createPropertyElement(iShape: IShape, onChange: (IShape) -> Unit): ReactElement<*>? {
            val mode = findMode(iShape) ?: return null

            val shapeSetter: (IShapePropertyElementProps<IShape>) -> Unit = {
                it.iShape = iShape
                it.onChange = onChange
            }

            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            return mode.propertyElement {
                (this as? IShapePropertyElementProps<IShape>)?.let(shapeSetter)
            }

        }
    }
}