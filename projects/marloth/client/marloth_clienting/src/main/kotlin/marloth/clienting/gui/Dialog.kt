package marloth.clienting.gui

import mythic.bloom.*
import mythic.bloom.next.*
import mythic.drawing.grayTone
import mythic.spatial.Vector4
import org.joml.Vector2i
import org.joml.plus
import scenery.enums.Text

val centeredDiv = reverseOffset(left = centered, top = centered)
val centerDialog = centeredDiv + shrink

const val titleContentHeight = 40

val titleBookendDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + titleContentHeight.toFloat() * 0.45f
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookend = FlexItem(div(depiction = titleBookendDepiction)(emptyFlower), FlexType.stretch)

fun titleBar(text: Text): Flower {
  return div(forward = forwardDimensions(height = fixed(titleContentHeight)), reverse = shrinkVertical)(
      margin(20)(
//          div(reverse = reverseOffset(left = centered) + shrink)(
//              label(TextStyles.mediumBlack, text)
//          )
          flexList(horizontalPlane, 10)(listOf(
              titleBookend,
              FlexItem(label(TextStyles.mediumBlack, text)),
              titleBookend
          ))
      )
  )
}

fun reversePair(plane: Plane, spacing: Int = 0, name: String = "reversePair"): (Pair<Flower, Flower>) -> Flower = { pair ->
  { seed ->
    val bottom = pair.second(seed)
    val bottomDimensions = plane(bottom.bounds.end)
    val childSeed = seed.copy(
        dimensions = plane(Vector2i(plane(seed.dimensions).x, bottomDimensions.y))
    )
    val top = pair.first(childSeed)
    val topDimensions = plane(top.bounds.end)
    val newSecond = bottom.copy(
        bounds = bottom.bounds.copy(
            position = bottom.bounds.position + plane(Vector2i(topDimensions.x + spacing, 0))
        )
    )
    val boxes = listOf(top, newSecond).reversed()
    Box(
        name = name,
        boxes = boxes,
        bounds = Bounds(
            dimensions = plane(Vector2i(topDimensions.x + bottomDimensions.x, bottomDimensions.y))
        )
    )
  }
}

fun dialog(title: Text): FlowerWrapper = { flower ->
  div(reverse = centerDialog, depiction = menuBackground)(
      reversePair(verticalPlane, 10)(Pair(
          titleBar(title),
          flower
      ))
  )
}
