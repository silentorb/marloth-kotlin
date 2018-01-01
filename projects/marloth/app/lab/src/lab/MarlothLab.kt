package lab

import mythic.spatial.Vector4

data class Box(
    val id: Int,
    val bounds: Vector4
)

typealias BoxMap = Map<Int, Box>

data class Border(
    val color: Vector4,
    val thickness: Float
)

data class Layout(
    val boxes: List<Box>,
    val borders: Map<Int, Border>
)

fun createLayout() = Layout(
    listOf(),
    mapOf()
)

fun drawBorder() {

}

fun createBoxMap(boxes: List<Box>): BoxMap = boxes.associate { Pair(it.id, it) }

fun renderLab() {
  val layout = createLayout()
  for (box in layout.boxes) {
    val border = layout.borders.get(box.id)
    if (border != null) {
      drawBorder()
    }
  }
}