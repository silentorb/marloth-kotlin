package marloth.clienting.menus

import marloth.clienting.hud.versionDisplay
import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

//val centeredDiv = reverseOffset(left = centered, top = centered)
//val centerDialog = centeredDiv + shrink

const val titleContentHeight = 40

val horizontalLine: Depiction = { bounds, canvas ->
  throw Error("Not implemented")
//  val middleY = bounds.top.toFloat() + bounds.dimensions.y
//  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookendDepiction: Depiction = { bounds, canvas ->
  throw Error("Not implemented")
//  val middleY = bounds.top.toFloat() + titleContentHeight.toFloat() * 0.45f
//  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

//val titleBookend = div(depiction = titleBookendDepiction)(emptyFlower)

val debugDepiction = solidBackground(Vector4(1f, 0f, 0f, 1f))

fun titleBar(text: String): SimpleLengthFlower =
    axisMargin(horizontalPlane, 20)(
        breadthList(verticalPlane)(
            listOf(
                flexList(horizontalPlane, spacing = 10,
                    children = listOf(
//                    flex2<HorizontalPlane>(titleBookend),
                        flex2(label(TextStyles.mediumBlack, text)),
//                    flex2<HorizontalPlane>(titleBookend)
                    )
                ),
//            div(forward = forwardDimensions(width = fixed(250), height = fixed(10)), reverse = reverseOffset(left = centered))(
//                div(depiction = horizontalLine)(emptyFlower)
//            )
            )
        )
    )

fun dialogContent(title: String): SimpleWildFlower = { box ->
  boxList(verticalPlane, 0)(
      listOf(
          titleBar(title)(box.dimensions.x),
          box
      )
  )
}

fun dialog(title: String): SimpleWildFlower = { box ->
//  div(reverse = centerDialog, depiction = menuBackground)(
  dialogContent(title)(box)
//  )
}

fun dialogWrapper(box: Box): SimpleFlower =
    centered(
        boxList(verticalPlane, 20)(
            listOf(
//                lengthToFlower(horizontalPlane)(
//                    centeredAxis(horizontalPlane)(
                Box(dimensions = Vector2i(500, 90), depiction = imageDepiction(UiTextures.marlothTitle))
//                    )
//                ),
                ,
                box.copy(depiction = menuBackground)
            )
        )
    )

fun dialogWrapperWithExtras(definitions: Definitions, box: Box): Flower =
    compose(
//        div(forward = stretchBoth)(
            depict(solidBackground(faintBlack))
//        ),
                ,
        versionDisplay(definitions.applicationInfo.version),
        dialogWrapper(box)
    )
