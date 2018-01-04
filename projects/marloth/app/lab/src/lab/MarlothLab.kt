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
    val boxes: List<Box>,
    val borders: Map<Box, Border>
)

fun createLabLayout(screenDimensions: Vector2): LabLayout {
  val (boxes, borders) = addBorders(createVerticalPanels(listOf(
      Measurement(Measurements.pixel, 200f),
      Measurement(Measurements.stretch, 0f),
      Measurement(Measurements.pixel, 200f)
  ), screenDimensions))

  return LabLayout(
      boxes,
      borders
  )
}

fun drawBorder(box: Box, border: Border, canvas: Canvas) {
  canvas.drawLineSquare(box.bounds.position, box.bounds.dimensions, border.color, 5f)
}

fun createBoxMap(boxes: List<Box>): BoxMap = boxes.associate { Pair(it, it) }

fun renderLab(layout: LabLayout, canvas: Canvas) {
  for (box in layout.boxes) {
    val border = layout.borders.get(box)
    if (border != null) {
      drawBorder(box, border, canvas)
    }
  }
}