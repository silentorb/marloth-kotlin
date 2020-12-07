package marloth.clienting.gui.menus

import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.hud.versionDisplay
import marloth.clienting.gui.menuBackground
import marloth.clienting.resources.UiTextures
import marloth.scenery.enums.Text
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

fun titleBar(title: Box): LengthFlower =
    breadthList(verticalPlane)(
        listOf(
            alignSingle(centered, horizontalPlane, title),
//            flexList(horizontalPlane, spacing = 10)(
//                listOf(
//                    flexFlower(titleBookend),
//                    flex(title),
//                    flexFlower(titleBookend),
//                )
//            ),
            marginSingle(horizontalPlane, all = 0, left = 30, right = 30)(
                horizontalLine
            )
        )
    )

fun dialogContent(title: Box): WildFlower = { box ->
  val result = boxMargin(20)(
      boxList(verticalPlane, 20,
          titleBar(title)(box.dimensions.x),
          box
      )
  ).copy(depiction = menuBackground)
  result
}

fun dialogTitle(text: String) =
    label(TextStyles.mediumBlack, text)

fun dialog(title: Box): WildFlower = { box ->
  dialogContent(title)(box)
}

fun dialog(title: String): WildFlower =
    dialog(dialogTitle(title))

fun dialog(definitions: Definitions, title: Text, box: Box): Box {
  val titleBox = dialogTitle(definitions.textLibrary(title))
  return dialogContent(titleBox)(box)
}

fun dialogWrapperWithExtras(definitions: Definitions, box: Box): Flower =
    compose(
        depict(solidBackground(faintBlack)),
        versionDisplay(definitions.applicationInfo.version),
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
    )

fun dialogWrapper(flower: StateFlower): StateFlowerTransform = { definitions, guiState ->
  centered(flower(definitions, guiState))
}

fun dialogWrapperWithExtras(flower: StateFlower): StateFlowerTransform = { definitions, guiState ->
  dialogWrapperWithExtras(definitions, flower(definitions, guiState))
}
