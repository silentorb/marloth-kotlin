package mythic.bloom

import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.plus
import org.lwjgl.opengl.GL11

enum class Measurements {
  stretch,
  pixel
}

data class Measurement(
    val type: Measurements,
    val value: Int
)

data class Bounds(
    val position: Vector2i = Vector2i(),
    val dimensions: Vector2i
) {
  constructor(x: Int, y: Int, width: Int, height: Int) :
      this(Vector2i(x, y), Vector2i(width, height))

  constructor(values: Vector4i) :
      this(Vector2i(values.x, values.y), Vector2i(values.z, values.w))

  val end: Vector2i = position + dimensions

  fun toVector4i() = Vector4i(position.x, position.y, dimensions.x, dimensions.y)
}

typealias Depiction = (Bounds, Canvas) -> Unit

typealias StateBag = Map<String, Any>

typealias StateBagMods = StateBag

data class BloomState(
    val bag: StateBag,
    val input: InputState
)

data class HistoricalBloomState(
    val bag: StateBag,
    val input: HistoricalInputState
)

typealias LogicModule = (HistoricalBloomState, Bounds) -> StateBagMods

data class Box(
    val bounds: Bounds,
    val depiction: Depiction? = null,
    val handler: Any? = null,
    val logic: LogicModule? = null
)

typealias Boxes = List<Box>

data class ClickBox<T>(
    val bounds: Bounds,
    val value: T
)

data class PartialBox(
    val length: Int,
    val depiction: Depiction? = null,
    val handler: Any? = null
)

fun crop(bounds: Bounds, canvas: Canvas, action: () -> Unit) = canvas.crop(bounds.toVector4i(), action)

fun resolveMeasurement(dimensions: Vector2i, plane: Plane, measurement: Measurement) =
    when (measurement.type) {
      Measurements.pixel -> measurement.value
//      Measurements.percent -> measurement.value * plane.x(dimensions) / 100
      Measurements.stretch -> null
    }

fun solveMeasurements(plane: Plane, lengths: List<Measurement>, bounds: Vector2i): List<Int> {
  val resolved = lengths.map { resolveMeasurement(bounds, plane, it) }
  val exacts = resolved.filterNotNull()

  val total = exacts.sum()
  val boundLength = plane.x(bounds)

  if (exacts.size == lengths.size) {
    if (total != boundLength)
      throw Error("Could not stretch or shrink to fit bounds")

    return exacts
  } else {
    val stretchCount = lengths.size - exacts.size
    val stretchLength = (boundLength - total) / stretchCount
    return resolved.map { if (it != null) it else stretchLength }
  }
}

fun listMeasuredBounds(plane: Plane, lengths: List<Measurement>, bounds: Vector2i): List<Bounds> {
  var progress = 0
  return solveMeasurements(plane, lengths, bounds).map {
    val b = Bounds(progress, 0, it, plane.y(bounds))
    progress += it
    b
  }
}

fun listBounds(plane: Plane, padding: Int, bounds: Bounds, lengths: List<Int>): List<Bounds> {
  var progress = 0
  val otherLength = plane.y(bounds.dimensions)

  return lengths.mapIndexed { i, length ->
    val b = Bounds(
        plane.vector(progress, 0) + bounds.position,
        plane.vector(length, otherLength)
    )
    progress += length
    if (i != lengths.size - 1)
      progress += padding

    b
  }
}

fun listContentLength(padding: Int, lengths: Collection<Int>): Int =
    lengths.sum() + (lengths.size + 1) * padding

typealias MeasuredLengthArrangement = (bounds: Vector2i, lengths: List<Measurement>) -> List<Bounds>
typealias LengthArrangement = (bounds: Bounds, lengths: List<Int>) -> List<Bounds>

val measuredHorizontalArrangement: MeasuredLengthArrangement = { bounds: Vector2i, lengths: List<Measurement> ->
  listMeasuredBounds(horizontalPlane, lengths, bounds)
}

fun horizontal(padding: Int): LengthArrangement = { bounds: Bounds, lengths: List<Int> ->
  listBounds(horizontalPlane, padding, bounds, lengths)
}

fun vertical(padding: Int): LengthArrangement = { bounds: Bounds, lengths: List<Int> ->
  listBounds(verticalPlane, padding, bounds, lengths)
}

fun arrange(plane: Plane, padding: Int, bounds: Bounds, lengths: List<Int?>): List<Bounds> =
    listBounds(plane, padding, bounds, resolveLengths(plane.x(bounds.dimensions), lengths))

fun vertical(padding: Int, bounds: Bounds, lengths: List<Int?>): List<Bounds> =
    arrange(verticalPlane, padding, bounds, lengths)

fun horizontal(padding: Int, bounds: Bounds, lengths: List<Int?>): List<Bounds> =
    arrange(horizontalPlane, padding, bounds, lengths)

fun arrangeMeasuredList(arrangement: MeasuredLengthArrangement, panels: List<Pair<Measurement, Depiction>>, bounds: Vector2i): List<Box> {
  return arrangement(bounds, panels.map { it.first })
      .zip(panels, { a, b -> Box(a, b.second) })
}

fun arrangeListComplex(arrangement: LengthArrangement, panels: List<PartialBox>, bounds: Bounds): List<Box> {
  return arrangement(bounds, panels.map { it.length })
      .zip(panels, { a, b -> Box(a, b.depiction) })
}

fun centeredPosition(boundsLength: Int, length: Int): Int =
    (boundsLength - length) / 2

fun centeredPosition(plane: Plane, bounds: Vector2i, length: Int?): Int =
    if (length == null)
      0
    else
      centeredPosition(plane.x(bounds), length)

fun centeredPosition(plane: Plane, bounds: Vector2i, length: Measurement): Int =
    centeredPosition(plane, bounds, resolveMeasurement(bounds, plane, length))

fun drawBorder(bounds: Bounds, canvas: Canvas, color: Vector4, thickness: Float = 1f) {
  canvas.drawSquare(bounds.position.toVector2(), bounds.dimensions.toVector2(), canvas.outline(color, thickness))
}

data class LineStyle(
    val color: Vector4,
    val thickness: Float
)

fun drawBorder(bounds: Bounds, canvas: Canvas, style: LineStyle) {
  canvas.drawSquare(bounds.position.toVector2(), bounds.dimensions.toVector2(), canvas.outline(style.color, style.thickness))
}

fun drawFill(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position.toVector2(), bounds.dimensions.toVector2(), canvas.solid(color))
}

fun applyBounds(bounds: Bounds, box: Box): Box =
    Box(Bounds(box.bounds.position + bounds.position, box.bounds.dimensions), box.depiction)

fun applyBounds(bounds: Bounds) = { box: Box -> applyBounds(bounds, box) }

typealias LayoutOld = List<Box>

fun renderLayout(layout: LayoutOld, canvas: Canvas) {
  globalState.depthEnabled = false
  globalState.blendEnabled = true
  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  for (box in layout) {
    val depiction = box.depiction
    if (depiction != null)
      depiction(box.bounds, canvas)
  }
}

fun centeredPosition(bounds: Bounds, contentDimensions: Vector2i): Vector2i {
  val dimensions = bounds.dimensions
  return bounds.position + Vector2i(
      centeredPosition(horizontalPlane, dimensions, contentDimensions.x),
      centeredPosition(verticalPlane, dimensions, contentDimensions.y)
  )
}

fun centeredBounds(bounds: Bounds, contentDimensions: Vector2i): Bounds {
  return Bounds(
      centeredPosition(bounds, contentDimensions) + bounds.position,
      contentDimensions
  )
}

fun isInBounds(position: Vector2i, bounds: Bounds): Boolean =
    position.x >= bounds.position.x &&
        position.x < bounds.position.x + bounds.dimensions.x &&
        position.y >= bounds.position.y &&
        position.y < bounds.position.y + bounds.dimensions.y

fun <T> filterMouseOverBoxes(boxes: List<ClickBox<T>>, mousePosition: Vector2i): ClickBox<T>? {
  return boxes.filter { box -> isInBounds(mousePosition, box.bounds) }.firstOrNull()
}

fun filterMouseOverBoxes(boxes: LayoutOld, mousePosition: Vector2i): Box? {
  return boxes
      .filter { it.handler != null }
      .filter { isInBounds(mousePosition, it.bounds) }.firstOrNull()
}

fun splitBoundsHorizontal(bounds: Bounds, leftPercentage: Float = 0.5f): Pair<Bounds, Bounds> {
  val leftWidth: Int = (bounds.dimensions.x * leftPercentage).toInt()
  return Pair(
      Bounds(
          position = bounds.position,
          dimensions = Vector2i(leftWidth, bounds.dimensions.y)
      ),
      Bounds(
          position = Vector2i(leftWidth, bounds.position.y),
          dimensions = Vector2i(bounds.dimensions.x - leftWidth, bounds.dimensions.y)
      )
  )
}

fun getEvent(layout: LayoutOld, mousePosition: Vector2i): Any? =
    filterMouseOverBoxes(layout, mousePosition)?.handler
