package lab

import generation.*
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.xy
import org.joml.plus
import org.joml.minus
import org.lwjgl.opengl.GL11
import randomly.Dice

val worldPadding = 20f // In screen units

typealias BoxMap = Map<Box, Box>

data class Border(
    val color: Vector4,
    val thickness: Float
)

private val panelBorder = Border(Vector4(0.2f, 0.2f, 1f, 1f), 3f)

fun addBorders(boxes: List<Box>) =
    Pair(boxes, boxes.associate { Pair(it, panelBorder) })

data class LabLayout(
    val boxes: List<Box>
)

fun <A, B, C> overlap(aList: List<A>, bList: List<B>, merger: (A, B) -> C): List<C> {
  val result = ArrayList<C>(aList.size)
  val iter = bList.iterator()
  for (a in aList) {
    result.add(merger(a, iter.next()))
  }
  return result
}

fun drawBorder(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.outline(color, 5f))
}

typealias PositionFunction = (Vector2) -> Vector2

//fun getPositionFunction(offset: Vector2, boundary: WorldBoundary, scale: Float): PositionFunction =
//    { position: Vector2 -> offset + (Vector2(position.x, -position.y) - boundary.start.xy) * scale }

fun getPositionFunction(offset: Vector2, boundary: WorldBoundary, scale: Float): PositionFunction {
  return { position: Vector2 -> offset + (Vector2(position.x, -position.y) - boundary.start.xy) * scale }
}

fun drawGeneratedWorld(bounds: Bounds, canvas: Canvas, abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  val scale = getScale(bounds, abstractWorld.boundary)
  val offset = bounds.position + worldPadding
  val getPosition: PositionFunction = getPositionFunction(offset, abstractWorld.boundary, scale)
  drawGrid(canvas, bounds, abstractWorld.boundary, scale)
  drawAbstractWorld(bounds, getPosition, canvas, abstractWorld)
  drawStructureWorld(bounds, getPosition, canvas, structureWorld)

  canvas.drawSquare(
      offset,
      abstractWorld.boundary.dimensions.xy * scale,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )
}

fun createLabLayout(abstractWorld: AbstractWorld, structureWorld: StructureWorld, screenDimensions: Vector2): LabLayout {
  val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Bounds, c: Canvas ->
    crop(b, c, { drawGeneratedWorld(b, c, abstractWorld, structureWorld) })
    draw(b, c)
  }

  val panels = listOf(
      Pair(Measurement(Measurements.pixel, 200f), draw),
      Pair(Measurement(Measurements.stretch, 0f), drawWorld),
      Pair(Measurement(Measurements.pixel, 200f), draw)
  )
  val boxes = overlap(createVerticalBounds(panels.map { it.first }, screenDimensions), panels, { a, b ->
    Box(a, b.second)
  })

  return LabLayout(
      boxes
  )
}

fun createBoxMap(boxes: List<Box>): BoxMap = boxes.associate { Pair(it, it) }

fun renderLab(layout: LabLayout, canvas: Canvas) {
  globalState.blendEnabled = true
  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  for (box in layout.boxes) {
    box.render(box.bounds, canvas)
  }
}

class MarlothLab {
  val abstractWorld = AbstractWorld(
      Vector3(-50f, -50f, -50f),
      Vector3(50f, 50f, 50f)
  )
  val structureWorld = StructureWorld()

  init {
    generateWorld(abstractWorld, structureWorld, Dice(1))
  }
}