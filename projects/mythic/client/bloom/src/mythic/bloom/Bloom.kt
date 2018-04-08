package mythic.bloom

import mythic.spatial.Vector2
import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.spatial.Vector4
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
//
//data class AxisMeasurement(
//    val near: Measurement,
//    val length: Measurement,
//    val far: Measurement
//)

//data class Bounds(
//    val x: AxisMeasurement,
//    val y: AxisMeasurement
//)
//
//enum class BoxType {
//
//}
//
//data class Box(
//    val bounds: Bounds,
////    val type: BoxType,
//    val parent: Box? = null
//)

data class Bounds(
    val position: Vector2,
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

}

class HorizontalPlane : Plane {
  override fun x(value: Vector2) = value.x
  override fun y(value: Vector2) = value.y

  override fun x(value: Vector2i) = value.x
  override fun y(value: Vector2i) = value.y
}

class VerticalPlane : Plane {
  override fun x(value: Vector2) = value.y
  override fun y(value: Vector2) = value.x

  override fun x(value: Vector2i) = value.y
  override fun y(value: Vector2i) = value.x
}

val horizontalPlane = HorizontalPlane()
val verticalPlane = VerticalPlane()

data class Box(
    val bounds: Bounds,
    val depiction: Depiction
) {
  constructor(x: Float, y: Float, width: Float, height: Float, depiction: Depiction) :
      this(Bounds(x, y, width, height), depiction)
}

fun crop(bounds: Bounds, canvas: Canvas, action: () -> Unit) = canvas.crop(bounds.toVector4i(), action)

fun resolveMeasurement(dimensions: Vector2, plane: Plane, measurement: Measurement) =
    when (measurement.type) {
      Measurements.pixel -> measurement.value
      Measurements.percent -> measurement.value * plane.x(dimensions) / 100
//      Measurements.shrink -> null
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

val listBounds = { plane: Plane, lengths: List<Measurement>, bounds: Vector2 ->
  var progress = 0f
  solveMeasurements(plane, lengths, bounds).map {
    val b = Bounds(progress, 0f, it, plane.y(bounds))
    progress += it
    b
  }
}

typealias LengthArrangement = (bounds: Vector2, lengths: List<Measurement>) -> List<Bounds>

val horizontalArrangement: LengthArrangement = { bounds: Vector2, lengths: List<Measurement> ->
  listBounds(horizontalPlane, lengths, bounds)
}

val verticalArrangement: LengthArrangement = { bounds: Vector2, lengths: List<Measurement> ->
  listBounds(verticalPlane, lengths, bounds)
}

val arrangeList = { arrangement: LengthArrangement, panels: List<Pair<Measurement, Depiction>>, bounds: Vector2 ->
  arrangement(bounds, panels.map { it.first })
      .zip(panels, { a, b -> Box(a, b.second) })
}

fun centeredPosition(boundsLength: Float, length: Float): Float =
    (boundsLength - length) / 2f

fun centeredPosition(plane: Plane, bounds: Vector2, length: Float?): Float =
    if (length == null)
      0f
    else
      centeredPosition(plane.x(bounds), length)

fun centeredPosition(plane: Plane, bounds: Vector2, length: Measurement): Float =
    centeredPosition(plane, bounds, resolveMeasurement(bounds, plane, length))

data class Layout(
    val boxes: List<Box>
)

fun drawBorder(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.outline(color, 1f))
}

fun drawFill(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.solid(color))
}

fun applyBounds(bounds: Bounds, box: Box): Box =
    Box(Bounds(box.bounds.position + bounds.position, box.bounds.dimensions), box.depiction)

fun applyBounds(bounds: Bounds) = { box: Box -> applyBounds(bounds, box) }

fun renderLayout(layout: Layout, canvas: Canvas) {
  globalState.depthEnabled = false
  globalState.blendEnabled = true
  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  for (box in layout.boxes) {
    box.depiction(box.bounds, canvas)
  }
}