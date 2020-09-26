package marloth.clienting.gui.menus

import marloth.clienting.StateFlower
import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.menus.logic.menuItemIndexKey
import marloth.clienting.gui.menus.logic.menuKey
import marloth.clienting.gui.menus.logic.onActivateKey
import marloth.clienting.gui.menus.logic.onClickKey
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import kotlin.math.max

typealias MenuItemFlower = (Boolean) -> Box

data class MenuItem(
    val flower: MenuItemFlower,
    val events: List<EventUnion> = listOf()
)

data class SimpleMenuItem(
    val text: Text,
    val events: List<EventUnion> = listOf()
)

fun newSimpleMenuItem(text: Text, event: EventUnion? = null) =
    SimpleMenuItem(
        text = text,
        events = listOfNotNull(event)
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

fun menuButtonWrapper(flower: MenuItemFlower): MenuItemFlower = { hasFocus ->
  flower(hasFocus)
}

fun fieldWrapper(focusIndex: Int, breadth: Int): (Int, Box) -> Box = { index, box ->
  val gap = 20
  val finalLength = breadth + gap * 2
  val wrapped = boxMargin(all = gap, top = 12)(
      box
  )
  Box(
      boxes = listOf(
          OffsetBox(
              child = wrapped,
              offset = Vector2i(centered(finalLength, wrapped.dimensions.x), 0)
          )
      ),
      dimensions = Vector2i(finalLength, wrapped.dimensions.y),
      depiction = drawMenuButtonBackground(index == focusIndex)
  )
}

fun menuFlower(menu: Menu, focusIndex: Int, titleWidth: Int): Box {
  val rows = menu
      .mapIndexed { index, item ->
        val hasFocus = index == focusIndex
        item.flower(hasFocus)
      }

  val gap = 20

  val breadth = boxList(verticalPlane, gap)(rows).dimensions.x
  return boxList(verticalPlane, gap)(
//      rows.mapIndexed(fieldWrapper(focusIndex, max(titleWidth + 100, breadth)))
      rows.mapIndexed { index, box ->
        val hasFocus = index == focusIndex
        val events = menu[index].events
        val attributes = if (hasFocus)
          mapOf(onActivateKey to events, onClickKey to events)
        else
          mapOf()

        fieldWrapper(focusIndex, max(titleWidth + 100, breadth))(index, box)
            .copy(
                attributes = attributes + (menuItemIndexKey to index)
            )
      }
  )
      .addAttributes(menuKey to menu)

}

val faintBlack = black.copy(w = 0.6f)

fun menuFlower(title: Box, menu: Menu): StateFlower = { definitions, state ->
  dialogContent(title)(menuFlower(menu, state.menuFocusIndex, title.dimensions.x))
}

fun menuFlower(title: Text, menu: Menu): StateFlower = { definitions, state ->
  menuFlower(dialogTitle(definitions.textLibrary(title)), menu)(definitions, state)
}

fun simpleMenuFlower(title: Text, source: List<SimpleMenuItem>): StateFlower = { definitions, state ->
  val menu = source.map {
    MenuItem(
        flower = menuTextFlower(definitions.textLibrary(it.text)),
        events = it.events
    )
  }
  menuFlower(title, menu)(definitions, state)
}
