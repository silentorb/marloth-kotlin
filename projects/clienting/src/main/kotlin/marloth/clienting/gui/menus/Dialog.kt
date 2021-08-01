package marloth.clienting.gui.menus

import marloth.clienting.ClientEventType
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.hud.versionDisplay
import marloth.clienting.gui.menuBackground
import marloth.clienting.gui.menus.general.addMenuItemInteractivity
import marloth.clienting.gui.menus.general.faintBlack
import marloth.clienting.resources.UiTextures
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.happenings.Command
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.toVector2
import simulation.misc.Definitions

const val titleContentHeight = 40

val horizontalLineDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + bounds.dimensions.y
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val horizontalLine: Flower = { seed ->
  Box(dimensions = Vector2i(seed.dimensions.x, 15), depiction = horizontalLineDepiction)
}

val titleBookendDepiction: Depiction = { bounds, canvas ->
  val middleY = bounds.top.toFloat() + titleContentHeight.toFloat() * 0.45f
  canvas.drawLine(bounds.left.toFloat(), middleY, bounds.right.toFloat(), middleY, black, 2f)
}

val titleBookend: Flower = depict(titleBookendDepiction)

val rotatedDepictionTransform: DepictionTransform = { b, c ->
  c.transformScalar(b.position.toVector2(), b.dimensions.toVector2())
      .translate(0.5f, 0.5f, 0f)
      .rotateZ(-Pi / 2f)
      .translate(-0.5f, -0.5f, 0f)
}

fun spadeDepiction() =
    addMenuItemInteractivity(0, listOf(Command(ClientEventType.menuBack))
    ) { seed ->
      val image = if (seed.state[menuItemIndexKey] == 0)
        UiTextures.cardSpadeHighlight
      else
        UiTextures.cardSpade

      Box(
          dimensions = Vector2i(32),
          depiction =
          imageDepiction(image, rotatedDepictionTransform)
      )
    }

fun titleBar(title: Box, showBackButton: Boolean = true): Flower =
    flowerList(verticalPlane)(
        listOf(
            alignSingleFlower(centered, horizontalPlane,
                if (showBackButton)
                  flowerList(horizontalPlane, 10)(
                      listOf(
                          spadeDepiction(),
                          title.toFlower()
                      )
                  )
                else
                  title.toFlower()
            ),
            flowerMargin(all = 0, left = 30, right = 30)(
                horizontalLine
            )
        )
    )

fun dialogContentFlower(title: Box): (Flower) -> Flower = { flower ->
  { seed ->
    val testTitlebar = titleBar(title)(seed)
    val box = flower(seed.copy(dimensions = floorDimensions(seed.dimensions - Vector2i(0, testTitlebar.dimensions.y + 20 + 20 * 2))))
    boxMargin(20)(
        boxList(verticalPlane, 20,
            titleBar(title)(seed.copy(dimensions = box.dimensions)),
            box
        )
    ).copy(depiction = menuBackground)
  }
}

fun dialogContent(box: Box) =
    boxMargin(20)(
        box
    ).copy(depiction = menuBackground)

fun dialogContentWithTitle(title: Flower): (Box) -> Flower = { box ->
  { seed ->
    dialogContent(
        boxList(verticalPlane, 20,
            title(seed.copy(dimensions = box.dimensions)),
            box
        )
    )
  }
}

fun dialogTitle(text: String) =
    label(TextStyles.mediumSemiBlackBold, text)

fun dialog(title: Box): WildFlower = { box ->
  dialogContentWithTitle(titleBar(title))(box)
}

fun dialog(title: String): WildFlower =
    dialog(dialogTitle(title))

fun dialog(definitions: Definitions, title: Text, box: Box): Flower {
  val titleBox = dialogTitle(definitions.textLibrary(title))
  return dialogContentWithTitle(titleBar(titleBox))(box)
}

fun dialogSurroundings(definitions: Definitions) =
    compose(
        depict(solidBackground(faintBlack)),
        versionDisplay(definitions.applicationInfo.version),
        silentOrbDisplay(),
    )

fun dialogHeader(box: Box) =
    alignListItems(verticalPlane, centered)(
        boxList(verticalPlane, 20)(
            listOf(
                Box(dimensions = Vector2i(500, 90), depiction = imageDepiction(UiTextures.marlothTitle)),
                box
            )
        )
    )

fun dialogWrapperWithExtras(definitions: Definitions, flower: Flower): Flower =
    compose(
        dialogSurroundings(definitions),
        centeredFlower { seed ->
          dialogHeader(flower(seed))
        }
    )

fun dialogWrapper(flower: StateFlower): StateFlower = { definitions, guiState ->
  centeredFlower(flower(definitions, guiState))
}

fun dialogWrapperWithExtras(flower: StateFlower): StateFlower = { definitions, guiState ->
  dialogWrapperWithExtras(definitions, flower(definitions, guiState))
}
