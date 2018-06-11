package mythic.bloom

import mythic.spatial.Vector2
import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.plus
import org.lwjgl.opengl.GL11

enum class Measurements {
  stretch,
  //  shrink,
  percent,
  pixel
}

data class Measurement(
    val type: Measurements,
    val value: Float
) {
  constructor(value: Float) : this(Measurements.pixel, value)
}

data class Bounds(
    val position: Vector2 = Vector2(),
    val dimensions: Vector2
) {
  constructor(x: Float, y: Float, width: Float, height: Float) :
      this(Vector2(x, y), Vector2(width, height))

  constructor(values: Vector4) :
      this(Vector2(values.x, values.y), Vector2(values.z, values.w))

  fun toVector4i() = Vector4i(position.x.toInt(), position.y.toInt(), dimensions.x.toInt(), dimensions.y.toInt())
}

typealias Depiction = (Bounds, Canvas) -> Unit

interface Plane {
  fun x(value: Vector2): Float
  fun y(value: Vector2): Float

  fun x(value: Vector2i): Int
  fun y(value: Vector2i): Int

  fun vector(first: Float, second: Float): Vector2
  fun vector(value: Vector2): Vector2
}

class HorizontalPlane : Plane {
  override fun x(value: Vector2) = value.x
  override fun y(value: Vector2) = value.y

  override fun x(value: Vector2i) = value.x
  override fun y(value: Vector2i) = value.y

  override fun vector(first: Float, second: Float): Vector2 = Vector2(first, second)

  override fun vector(value: Vector2): Vector2 = value
}

class VerticalPlane : Plane {
  override fun x(value: Vector2) = value.y
  override fun y(value: Vector2) = value.x

  override fun x(value: Vector2i) = value.y
  override fun y(value: Vector2i) = value.x

  override fun vector(first: Float, second: Float): Vector2 = Vector2(second, first)
  override fun vector(value: Vector2): Vector2 = Vector2(value.y, value.x)
}

val horizontalPlane = HorizontalPlane()
val verticalPlane = VerticalPlane()

data class Box(
    val bounds: Bounds,
    val depiction: Depiction? = null,
    val handler: Any? = null
)

data class ClickBox<T>(
    val bounds: Bounds,
    val value: T
)

data class PartialBox(
    val length: Float,
    val depiction: Depiction? = null,
    val handler: Any? = null
)

fun crop(bounds: Bounds, canvas: Canvas, action: () -> Unit) = canvas.crop(bounds.toVector4i(), action)

fun resolveMeasurement(dimensions: Vector2, plane: Plane, measurement: Measurement) =
    when (measurement.type) {
      Measurements.pixel -> measurement.value
      Measurements.percent -> measurement.value * plane.x(dimensions) / 100
      Measurements.stretch -> null
    }

fun solveMeasurements(plane: Plane, lengths: List<Measurement>, bounds: Vector2): List<Float> {
  val resolved = lengths.map { resolveMeasurement(bounds, plane, it) }
  val exacts = resolved.filterNotNull()

  val total = exacts.fold(0f, { a, b -> a + b })
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

fun resolveLengths(boundLength: Float, lengths: List<Float?>): List<Float> {
  val exacts = lengths.filterNotNull()
  val total = exacts.fold(0f, { a, b -> a + b })

  if (exacts.size == lengths.size) {
    if (total != boundLength)
      throw Error("Could not stretch or shrink to fit bounds")

    return exacts
  } else {
    val stretchCount = lengths.size - exacts.size
    val stretchLength = (boundLength - total) / stretchCount
    return lengths.map { if (it != null) it else stretchLength }
  }
}

fun listMeasuredBounds(plane: Plane, lengths: List<Measurement>, bounds: Vector2): List<Bounds> {
  var progress = 0f
  return solveMeasurements(plane, lengths, bounds).map {
    val b = Bounds(progress, 0f, it, plane.y(bounds))
    progress += it
    b
  }
}

fun listBounds(plane: Plane, padding: Vector2, bounds: Bounds, lengths: List<Float>): List<Bounds> {
  val mainPadding = plane.x(padding)
  var progress = mainPadding
  val otherPadding = plane.y(padding)
  val otherLength = plane.y(bounds.dimensions) - otherPadding * 2

  return lengths.map { length ->
    val b = Bounds(
        plane.vector(progress, otherPadding) + bounds.position,
        plane.vector(length, otherLength)
    )
    progress += length + mainPadding
    b
  }
}

fun listContentLength(padding: Float, lengths: Collection<Float>): Float =
    lengths.sum() + (lengths.size + 1) * padding
//
//fun listLengths(padding: Float, lengths: Collection<Float>): List<Float> {
//  var offset = 0f
//  return lengths.map {
//    val result = offset
//    offset += padding + it
//    result
//  }
//}

typealias MeasuredLengthArrangement = (bounds: Vector2, lengths: List<Measurement>) -> List<Bounds>
typealias LengthArrangement = (bounds: Bounds, lengths: List<Float>) -> List<Bounds>

val measuredHorizontalArrangement: MeasuredLengthArrangement = { bounds: Vector2, lengths: List<Measurement> ->
  listMeasuredBounds(horizontalPlane, lengths, bounds)
}

//val measuredVerticalArrangement: MeasuredLengthArrangement = { bounds: Vector2, lengths: List<Measurement> ->
//  listMeasuredBounds(verticalPlane, lengths, bounds)
//}

fun arrangeHorizontal(padding: Vector2): LengthArrangement = { bounds: Bounds, lengths: List<Float> ->
  listBounds(horizontalPlane, padding, bounds, lengths)
}

fun arrangeVertical(padding: Vector2): LengthArrangement = { bounds: Bounds, lengths: List<Float> ->
  listBounds(verticalPlane, padding, bounds, lengths)
}

fun arrange(plane: Plane, padding: Vector2, bounds: Bounds, lengths: List<Float?>): List<Bounds> =
    listBounds(plane, padding, bounds, resolveLengths(plane.x(bounds.dimensions), lengths))

fun arrangeVertical(padding: Vector2, bounds: Bounds, lengths: List<Float?>): List<Bounds> =
    arrange(verticalPlane, padding, bounds, lengths)

fun arrangeHorizontal(padding: Vector2, bounds: Bounds, lengths: List<Float?>): List<Bounds> =
    arrange(horizontalPlane, padding, bounds, lengths)

//fun arrangeVertical(padding: Float, bounds: Bounds, lengths: List<Float>) =
//    listLengths(verticalPlane, padding, lengths, bounds)

fun arrangeMeasuredList(arrangement: MeasuredLengthArrangement, panels: List<Pair<Measurement, Depiction>>, bounds: Vector2): List<Box> {
  return arrangement(bounds, panels.map { it.first })
      .zip(panels, { a, b -> Box(a, b.second) })
}

fun arrangeListComplex(arrangement: LengthArrangement, panels: List<PartialBox>, bounds: Bounds): List<Box> {
  return arrangement(bounds, panels.map { it.length })
      .zip(panels, { a, b -> Box(a, b.depiction) })
}

//fun arrangeList(arrangement: LengthArrangement, panels: List<Float>, bounds: Bounds): List<Bounds> {
//  return arrangement(bounds, panels)
////      .zip(panels, { a, b -> Box(a, b.depiction) })
//}

fun centeredPosition(boundsLength: Float, length: Float): Float =
    (boundsLength - length) / 2f

fun centeredPosition(plane: Plane, bounds: Vector2, length: Float?): Float =
    if (length == null)
      0f
    else
      centeredPosition(plane.x(bounds), length)

fun centeredPosition(plane: Plane, bounds: Vector2, length: Measurement): Float =
    centeredPosition(plane, bounds, resolveMeasurement(bounds, plane, length))

fun drawBorder(bounds: Bounds, canvas: Canvas, color: Vector4, thickness: Float = 1f) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.outline(color, thickness))
}

data class LineStyle(
    val color: Vector4,
    val thickness: Float
)

fun drawBorder(bounds: Bounds, canvas: Canvas, style: LineStyle) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.outline(style.color, style.thickness))
}

fun drawFill(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.solid(color))
}

fun applyBounds(bounds: Bounds, box: Box): Box =
    Box(Bounds(box.bounds.position + bounds.position, box.bounds.dimensions), box.depiction)

fun applyBounds(bounds: Bounds) = { box: Box -> applyBounds(bounds, box) }

typealias Layout = List<Box>

fun renderLayout(layout: Layout, canvas: Canvas) {
  globalState.depthEnabled = false
  globalState.blendEnabled = true
  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  for (box in layout) {
    if (box.depiction != null)
      box.depiction!!(box.bounds, canvas)
  }
}

fun centeredPosition(bounds: Bounds, contentDimensions: Vector2): Vector2 {
  val dimensions = bounds.dimensions
  return bounds.position + Vector2(
      centeredPosition(horizontalPlane, dimensions, contentDimensions.x),
      centeredPosition(verticalPlane, dimensions, contentDimensions.y)
  )
}

fun centeredBounds(bounds: Bounds, contentDimensions: Vector2): Bounds {
  return Bounds(
      centeredPosition(bounds, contentDimensions) + bounds.position,
      contentDimensions
  )
}

fun isInBounds(position: Vector2, bounds: Bounds): Boolean =
    position.x >= bounds.position.x &&
        position.x < bounds.position.x + bounds.dimensions.x &&
        position.y >= bounds.position.y &&
        position.y < bounds.position.y + bounds.dimensions.y

fun <T> filterMouseOverBoxes(boxes: List<ClickBox<T>>, mousePosition: Vector2i): ClickBox<T>? {
  val position = mousePosition.toVector2()
  return boxes.filter { box -> isInBounds(position, box.bounds) }.firstOrNull()
}

fun filterMouseOverBoxes(boxes: Layout, mousePosition: Vector2i): Box? {
  val position = mousePosition.toVector2()
  return boxes
      .filter { it.handler != null }
      .filter { isInBounds(position, it.bounds) }.firstOrNull()
}

fun splitBoundsHorizontal(bounds: Bounds, leftPercentage: Float = 0.5f): Pair<Bounds, Bounds> {
  val leftWidth = bounds.dimensions.x * leftPercentage
  return Pair(
      Bounds(
          position = bounds.position,
          dimensions = Vector2(leftWidth, bounds.dimensions.y)
      ),
      Bounds(
          position = Vector2(leftWidth, bounds.position.y),
          dimensions = Vector2(bounds.dimensions.x - leftWidth, bounds.dimensions.y)
      )
  )
}

fun getEvent(layout: Layout, mousePosition: Vector2i): Any? =
    filterMouseOverBoxes(layout, mousePosition)?.handler
