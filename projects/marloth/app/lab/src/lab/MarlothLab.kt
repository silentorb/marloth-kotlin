package lab

import generation.AbstractWorld
import generation.Generator
import generation.Node
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.FillType
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import org.joml.plus
import org.joml.xy
import randomly.Dice

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

fun drawBorder(box: Box, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(box.bounds.position, box.bounds.dimensions, canvas.outline(color, 5f))
}

fun drawAbstractWorld(box: Box, canvas: Canvas, world: AbstractWorld) {
  val solid = canvas.solid(Vector4(1f, 1f, 0f, 1f))
  val outline = canvas.outline(Vector4(1f, 0f, 0f, 1f), 5f)
  fun getPosition(node: Node) = box.bounds.position + node.position.xy

  for (node in world.nodes) {
    val radius = 20f
    val position = getPosition(node)
    canvas.drawSolidCircle(position, radius, solid)
    canvas.drawCircle(position, radius, outline)
  }

  for (connection in world.connections) {
    canvas.drawLine(getPosition(connection.first), getPosition(connection.second), Vector4(0f, 0.6f, 0f, 1f), 5f)
  }
}

fun createLabLayout(world: AbstractWorld, screenDimensions: Vector2): LabLayout {
  val draw = { b: Box, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Box, c: Canvas ->
    draw(b, c)
    drawAbstractWorld(b, c, world)
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
  for (box in layout.boxes) {
    box.render(box, canvas)
  }
}

class MarlothLab {
  val generator: Generator = Generator(AbstractWorld(), Dice(1))

  init {
    generator.generate()
  }
}