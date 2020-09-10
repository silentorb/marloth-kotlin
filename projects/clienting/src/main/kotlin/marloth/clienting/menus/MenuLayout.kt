package marloth.clienting.menus

import marloth.clienting.StateFlower
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4

typealias MenuItemFlower = (Boolean) -> Box

data class MenuItem(
    val flower: MenuItemFlower,
    val event: ClientOrServerEvent? = null
)

data class SimpleMenuItem(
    val text: Text,
    val event: ClientOrServerEvent? = null,
    val command: GuiCommandType? = null
)

typealias Menu = List<MenuItem>

fun getFocusStyle(hasFocus: Boolean) =
    if (hasFocus)
      Pair(TextStyles.mediumBlack, LineStyle(Vector4(1f), 2f))
    else
      Pair(TextStyles.mediumBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

fun drawMenuButtonBorder(hasFocus: Boolean, bounds: Bounds, canvas: Canvas) {
  val style = getFocusStyle(hasFocus)
  drawBorder(bounds, canvas, style.second)
}

fun menuTextFlower(text: String): MenuItemFlower = { hasFocus ->
  val style = getFocusStyle(hasFocus)
  label(style.first, text)
}

fun drawMenuButtonBackground(hasFocus: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
//  drawFill(bounds, canvas, grayTone(0.5f))
  drawMenuButtonBorder(hasFocus, bounds, canvas)
}

private val buttonDimensions = Vector2i(200, 50)

//fun menuButton(flower: MenuItemFlower, hasFocus: Boolean): Flower = { dimensions: Seed ->
//  flower(hasFocus)(dimensions)
//}

fun menuButtonWrapper(flower: MenuItemFlower): MenuItemFlower = { hasFocus ->
//  div(
//      name = "simple menu button",
//      forward = forwardDimensions(buttonDimensions),
//      reverse = shrink,
////        depiction = drawMenuButtonBackground(hasFocus)
//  )(flower(hasFocus))(dimensions)
  flower(hasFocus)
}

fun menuListWrapper(plane: Plane, wrapper: IndexedFlowerWrapper): FlowerContainerWrapper = { container ->
  { items ->
    { dimensions ->
      val boxes = items.map { it(dimensions) }
      val breadth = getListBreadth(plane, boxes)
      val childSeed = plane(Vector2i(plane(dimensions).x, breadth))
      container(items.mapIndexed(wrapper))(childSeed)
    }
  }
}

fun menuFlower(menu: Menu, focusIndex: Int): Flower {
  val rows = menu
      .mapIndexed { index, item ->
        val hasFocus = index == focusIndex
        val box = item.flower(hasFocus)
        val event = item.event
        val attributes = if (hasFocus)
          mapOf(onActivateKey to event, onClickKey to event)
        else
          mapOf()

        withAttributes(attributes + (menuItemIndexKey to index), box)
      }

  val gap = 20

  val wrapper: IndexedFlowerWrapper = { index, flower ->
    div(
        reverse = shrinkVertical,
        depiction = drawMenuButtonBackground(index == focusIndex)
    )(
        boxToFlower(
            reverseMargin(all = gap)(
                flowerToBox(
                    div(
                        reverse = shrink + reverseOffset(left = centered),
                    )(flower)
                )
            )
        )
    )
  }
  return div(
      reverse = shrink,
      attributes = mapOf(menuKey to menu)
  )(
      boxToFlower(
          reverseMargin(all = gap)(
//      menuListWrapper(verticalPlane, wrapper)(list(verticalPlane, gap))(rows)
              list(verticalPlane, gap)(rows)
          )
      )
  )

}

val faintBlack = black.copy(w = 0.6f)

fun menuFlower(title: Text, menu: Menu): StateFlower = { definitions, state ->
  commonDialog(definitions, definitions.textLibrary(title), menuFlower(menu, state.menuFocusIndex))
}

fun simpleMenuFlower(title: Text, source: List<SimpleMenuItem>): StateFlower = { definitions, state ->
  val menu = source.map {
    MenuItem(
        flower = menuButtonWrapper(menuTextFlower(definitions.textLibrary(it.text))),
        event = it.event ?: clientEvent(it.command!!)
    )
  }
  menuFlower(title, menu)(definitions, state)
}
