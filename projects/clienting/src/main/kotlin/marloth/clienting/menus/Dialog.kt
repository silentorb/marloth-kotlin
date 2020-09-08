package marloth.clienting.menus

import marloth.clienting.hud.versionDisplay
import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.Vector2i
import marloth.scenery.enums.Text
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

fun titleBar(text: Text): Flower {
  return margin(20)(
      list(verticalPlane)(listOf(
          flexList(horizontalPlane, 10)(listOf(
              titleBookend,
              FlexItem(localizedLabel(TextStyles.mediumBlack, text)),
              titleBookend
          )),
          div(forward = forwardDimensions(width = fixed(250), height = fixed(10)), reverse = reverseOffset(left = centered))(
              div(depiction = horizontalLine)(emptyFlower)
          )
      ))
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
        bounds = Bounds(
            dimensions = plane(Vector2i(topDimensions.x + bottomDimensions.x, bottomDimensions.y))
        ),
        boxes = boxes
    )
  }
}

fun dialogContent(title: Text): FlowerWrapper = { flower ->
  reversePair(verticalPlane, 0)(Pair(
      titleBar(title),
      flower
  ))
}

fun dialog(title: Text): FlowerWrapper = { flower ->
  div(reverse = centerDialog, depiction = menuBackground)(
      dialogContent(title)(flower)
  )
}

fun commonDialog(definitions: Definitions, title: Text, flower: Flower) =
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
