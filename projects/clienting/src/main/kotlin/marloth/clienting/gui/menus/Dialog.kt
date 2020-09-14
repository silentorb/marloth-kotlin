package marloth.clienting.gui.menus

import marloth.clienting.gui.hud.versionDisplay
import marloth.clienting.gui.menuBackground
import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

const val titleContentHeight = 40

val horizontalLineDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + bounds.dimensions.y
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val horizontalLine: LengthFlower = { length ->
  Box(dimensions = Vector2i(length, 15), depiction = horizontalLineDepiction)
}

val titleBookendDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + titleContentHeight.toFloat() * 0.45f
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookend: Flower = depict(titleBookendDepiction)

fun titleBar(text: String): LengthFlower =
    breadthList(verticalPlane)(
        listOf(
            flexList(horizontalPlane, spacing = 10)(
                listOf(
                    flexFlower(titleBookend),
                    flex(label(TextStyles.mediumBlack, text)),
                    flexFlower(titleBookend),
                )
            ),
            axisMargin(horizontalPlane, all = 0, left = 30, right = 30)(
                horizontalLine
            )
        )
    )

fun dialogContent(title: String): WildFlower = { box ->
  boxMargin(20)(
      boxList(verticalPlane, 20,
          titleBar(title)(box.dimensions.x),
          box
      )
  ).copy(depiction = menuBackground)
}

fun dialog(title: String): WildFlower = { box ->
  dialogContent(title)(box)
}

fun dialogWrapper(box: Box): Flower =
    centered(
        alignListItems(verticalPlane, centered)(
            boxList(verticalPlane, 20)(
                listOf(
                    Box(dimensions = Vector2i(500, 90), depiction = imageDepiction(UiTextures.marlothTitle)),
                    box
                )
            )
        )
    )

fun dialogWrapperWithExtras(definitions: Definitions, box: Box): Flower =
    compose(
        depict(solidBackground(faintBlack)),
        versionDisplay(definitions.applicationInfo.version),
        dialogWrapper(box)
    )
