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

val titleBookend = div(depiction = titleBookendDepiction)(emptyFlower)

val debugDepiction = solidBackground(Vector4(1f, 0f, 0f, 1f))

fun titleBar(text: String): HorizontalLengthFlower =
    axisMargin<HorizontalPlane>(20, child =
    list<HorizontalPlane>(children = listOf(
        flexList<HorizontalPlane>(spacing = 10, children =
        listOf(
//                    flex2<HorizontalPlane>(titleBookend),
            flex2<HorizontalPlane>(label(TextStyles.mediumBlack, text)),
//                    flex2<HorizontalPlane>(titleBookend)
        )
        ),
//            div(forward = forwardDimensions(width = fixed(250), height = fixed(10)), reverse = reverseOffset(left = centered))(
//                div(depiction = horizontalLine)(emptyFlower)
//            )
    )
    )
    )

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
    val boxes = listOf(newSecond, top)
    Box(
        name = name,
        bounds = Bounds(
            dimensions = plane(Vector2i(topDimensions.x + bottomDimensions.x, bottomDimensions.y))
        ),
        boxes = boxes
    )
  }
}

fun dialogContent(title: String): WildFlower = { box ->
  list(verticalPlane, 0)(
      listOf(
          titleBar(title)(box.dimensions.x),
          box
      )
  )
}

fun dialog(title: String): WildFlower = { box ->
//  div(reverse = centerDialog, depiction = menuBackground)(
  dialogContent(title)(box)
//  )
}

fun dialogWrapper(box: Box) =
    div(reverse = centerDialog)(
        reversePair(verticalPlane, 20)(
            Pair(
                lengthToFlower<HorizontalPlane>(
                    centeredAxis<HorizontalPlane>(
                        SimpleBox(dimensions = Vector2i(500, 90), depiction = imageDepiction(UiTextures.marlothTitle))
                    )
                ),
//                div(reverse = reverseOffset(left = centered), forward = forwardDimensions(fixed(500), fixed(90)))(
//                    imageElement(UiTextures.marlothTitle)
//                ),
                boxToFlower(box.copy(depiction = menuBackground))
            )
        )
    )

fun dialogWrapperWithExtras(definitions: Definitions, box: Box): Flower =
    compose(
        div(forward = stretchBoth)(
            depict(solidBackground(faintBlack))
        ),
        versionDisplay(definitions.applicationInfo.version),
        dialogWrapper(box)
    )
