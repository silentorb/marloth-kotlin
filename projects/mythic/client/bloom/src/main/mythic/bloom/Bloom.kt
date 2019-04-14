package mythic.bloom

import mythic.drawing.Canvas
import mythic.glowing.getGLBounds
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.plus
import org.lwjgl.opengl.GL11

data class Bounds(
    val position: Vector2i = Vector2i(),
    val dimensions: Vector2i
) {
  constructor(x: Int, y: Int, width: Int, height: Int) :
      this(Vector2i(x, y), Vector2i(width, height))

  constructor(values: Vector4i) :
      this(Vector2i(values.x, values.y), Vector2i(values.z, values.w))

  val left: Int = position.x
  val top: Int = position.y
  val right: Int = position.x + dimensions.x
  val bottom: Int = position.y + dimensions.y

  val end: Vector2i = position + dimensions

  fun toVector4i() = Vector4i(position.x, position.y, dimensions.x, dimensions.y)

  companion object {
    fun fromEnds(left: Int, top: Int, right: Int, bottom: Int) =
        Bounds(left, top, right - left, bottom - top)
  }
}

val emptyBounds = Bounds(0, 0, 0, 0)

typealias Depiction = (Bounds, Canvas) -> Unit
typealias StateBag = Map<String, Any>
typealias StateBagMods = StateBag

data class Box(
    val bounds: Bounds,
    val depiction: Depiction? = null,
    val clipBounds: Bounds? = null,
    val handler: Any? = null,
    val logic: LogicModule? = null
)

typealias Boxes = List<Box>

fun crop(bounds: Bounds, canvas: Canvas, action: () -> Unit) = canvas.crop(bounds.toVector4i(), action)

fun listBounds(plane: Plane, padding: Int, bounds: Bounds, lengths: List<Int>): List<Bounds> {
  var progress = 0
  val otherLength = plane(bounds.dimensions).y

  return lengths.mapIndexed { i, length ->
    val b = Bounds(
        plane(Vector2i(progress, 0)) + bounds.position,
        plane(Vector2i(length, otherLength))
    )
    progress += length
    if (i != lengths.size - 1)
      progress += padding

    b
  }
}

typealias LengthArrangement = (bounds: Bounds, lengths: List<Int>) -> List<Bounds>

fun lengthArranger(plane: Plane, padding: Int): LengthArrangement = { bounds: Bounds, lengths: List<Int> ->
  listBounds(plane, padding, bounds, lengths)
}

fun fixedLengthArranger(plane: Plane, padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  lengthArranger(plane, padding)(bounds, resolveLengths(plane(bounds.dimensions).x, lengths))
}

fun boundsArranger(plane: Plane, padding: Int): (Bounds, List<Bounds>) -> List<Bounds> = { bounds, b ->
  val lengths = b.map { plane(it.dimensions).x }
  listBounds(plane, padding, bounds, lengths)
}

fun centeredPosition(boundsLength: Int, length: Int): Int =
    (boundsLength - length) / 2

fun centeredPosition(plane: Plane, bounds: Vector2i, length: Int?): Int =
    if (length == null)
      0
    else
      centeredPosition(plane(bounds).x, length)

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

typealias LayoutOld = List<Box>

fun renderLayout(layout: LayoutOld, canvas: Canvas) {
  val current = getGLBounds(GL11.GL_VIEWPORT)
  if (current.z == 0)
    return

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
      centeredPosition(horizontal, dimensions, contentDimensions.x),
      centeredPosition(vertical, dimensions, contentDimensions.y)
  )
}

fun centeredBounds(bounds: Bounds, contentDimensions: Vector2i): Bounds {
  return Bounds(
      centeredPosition(bounds, contentDimensions) + bounds.position,
      contentDimensions
  )
}
