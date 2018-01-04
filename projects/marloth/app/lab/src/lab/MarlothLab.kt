package lab

import mythic.drawing.Canvas
import mythic.drawing.FillType
import mythic.spatial.Vector2
import mythic.spatial.Vector4

data class Bounds(
    val position: Vector2,
    val dimensions: Vector2
) {
  constructor(x: Float, y: Float, width: Float, height: Float) :
      this(Vector2(x, y), Vector2(width, height))
}

data class Box(
    val bounds: Bounds
)

typealias BoxMap = Map<Box, Box>

data class Border(
    val color: Vector4,
    val thickness: Float
)

data class LabLayout(
    val boxes: List<Box>,
    val borders: Map<Box, Border>
)

private val panelBorder = Border(Vector4(0.2f, 0.2f, 1f, 1f), 3f)

data class VerticalPanel (

)

fun createVerticalPanels(panels: List<VerticalPanel>, height: Float): List<Box> = panels.map { Box()}

fun createLabLayout() = LabLayout(
    listOf(
        Box(Bounds(50f, 150f, 200f, 100f))
    ),
    mapOf(
        1 to panelBorder
    )
)

fun drawBorder(box: Box, border: Border, canvas: Canvas) {
  canvas.drawLineSquare(box.bounds.position, box.bounds.dimensions, border.color, 5f)
}

fun createBoxMap(boxes: List<Box>): BoxMap = boxes.associate { Pair(it.id, it) }

fun renderLab(layout: LabLayout, canvas: Canvas) {
  for (box in layout.boxes) {
    val border = layout.borders.get(box)
    if (border != null) {
      drawBorder(box, border, canvas)
    }
  }
}