package marloth.clienting.menus

import marloth.clienting.hud.versionDisplay
import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

val centeredDiv = reverseOffset(left = centered, top = centered)
val centerDialog = centeredDiv + shrink

const val titleContentHeight = 40

val horizontalLine: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + bounds.dimensions.y
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookendDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + titleContentHeight.toFloat() * 0.45f
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookendFlower = div(depiction = titleBookendDepiction)(emptyFlower)

val titleBookend = FlexItem(titleBookendFlower, FlexType.stretch)
val debugDepiction = solidBackground(Vector4(1f, 0f, 0f, 1f))

fun titleBar(text: String): Box {
  return reverseMargin(20)(
      list(verticalPlane)(
          listOf(
              flowerToBox(
                  flexList(horizontalPlane, 10)(listOf(
                      titleBookend,
                      FlexItem(boxToFlower(label(TextStyles.mediumBlack, text))),
                      titleBookend
                  ))
              ),
              flowerToBox(
                  div(forward = forwardDimensions(width = fixed(250), height = fixed(10)), reverse = reverseOffset(left = centered))(
                      div(depiction = horizontalLine)(emptyFlower)
                  )
              )
          )
      )
  )
}

fun reversePair(plane: Plane, spacing: Int = 0, name: String = "reversePair"): (Pair<Flower, Flower>) -> Flower = { pair ->
  { dimensions ->
    val bottom = pair.second(dimensions)
    val bottomDimensions = plane(bottom.bounds.end)
    val childSeed = plane(Vector2i(plane(dimensions).x, bottomDimensions.y))
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
        bounds = Bounds(
            dimensions = plane(Vector2i(topDimensions.x + bottomDimensions.x, bottomDimensions.y))
        ),
        boxes = boxes
    )
  }
}

fun dialogContent(title: String): FlowerWrapper = { flower ->
  reversePair(verticalPlane, 0)(Pair(
      boxToFlower(titleBar(title)),
      flower
  ))
}

fun dialog(title: String): FlowerWrapper = { flower ->
  div(reverse = centerDialog, depiction = menuBackground)(
      dialogContent(title)(flower)
  )
}

fun commonDialog(definitions: Definitions, title: String, flower: Flower) =
    compose(
        div(forward = stretchBoth)(
            depict(solidBackground(faintBlack))
        ),
        versionDisplay(definitions.applicationInfo.version),
        div(reverse = centerDialog)(
            reversePair(verticalPlane, 20)(
                Pair(
                    div(reverse = reverseOffset(left = centered), forward = forwardDimensions(fixed(500), fixed(90)))(
                        imageElement(UiTextures.marlothTitle)
                    ),
                    div(reverse = shrink, depiction = menuBackground)(
                        dialogContent(title)(flower)
                    )
                )
            )
        )
    )
