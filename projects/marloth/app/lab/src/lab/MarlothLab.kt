package lab

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.FillType
import mythic.spatial.Vector2
import mythic.spatial.Vector4

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

fun createLabLayout(screenDimensions: Vector2): LabLayout {
  val draw = { a: Box, b: Canvas -> drawBorder(a, b, Vector4(0f, 0f, 1f, 1f)) }
  val panels = listOf(
      Pair(Measurement(Measurements.pixel, 200f), draw),
      Pair(Measurement(Measurements.stretch, 0f), draw),
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