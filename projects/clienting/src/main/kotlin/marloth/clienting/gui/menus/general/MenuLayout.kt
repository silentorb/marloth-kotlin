package marloth.clienting.gui.menus.general

import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.black
import marloth.clienting.gui.menus.dialogContent
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.logic.menuItemIndexKey
import marloth.clienting.gui.menus.logic.menuKey
import marloth.clienting.gui.menus.logic.onActivateKey
import marloth.clienting.gui.menus.logic.onClickKey
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.IndexedTextStyle
import simulation.misc.Definitions
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

fun getFocusStyle(hasFocus: Boolean, enabled: Boolean): Pair<IndexedTextStyle, LineStyle> {
  val textStyle = if (enabled)
    TextStyles.mediumBlack
  else
    TextStyles.gray

  return if (hasFocus)
    textStyle to LineStyle(Vector4(1f), 2f)
  else
    textStyle to LineStyle(Vector4(0f, 0f, 0f, 1f), 1f)
}

fun drawMenuButtonBorder(hasFocus: Boolean, enabled: Boolean, bounds: Bounds, canvas: Canvas) {
  val style = getFocusStyle(hasFocus, enabled)
  drawBorder(bounds, canvas, style.second)
}

fun menuTextFlower(text: String, enabled: Boolean = true): MenuItemFlower = { hasFocus ->
  val style = getFocusStyle(hasFocus, enabled)
  label(style.first, text)
}

fun drawMenuButtonBackground(hasFocus: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawMenuButtonBorder(hasFocus, true, bounds, canvas)
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


fun layoutMenuItems(menu: Menu, focusIndex: Int): List<Box> {
  return menu
      .mapIndexed { index, item ->
        val hasFocus = index == focusIndex
        item.flower(hasFocus)
      }
}

const val defaultMenuGap = 20

fun layoutMenuItems(rows: List<Box>, delegate: (Int, Box) -> Box): Box {
  return boxList(verticalPlane, defaultMenuGap)(
      rows.mapIndexed(delegate)
  )
}

fun menuFlower(menu: Menu, focusIndex: Int, minWidth: Int): Box {
  val rows = layoutMenuItems(menu, focusIndex)

  val breadth = boxList(verticalPlane, defaultMenuGap)(rows).dimensions.x
  return layoutMenuItems(rows) { index, box ->
    val hasFocus = index == focusIndex
    val events = menu[index].events
    val attributes = if (hasFocus)
      mapOf(onActivateKey to events, onClickKey to events)
    else
      mapOf()

    fieldWrapper(focusIndex, max(minWidth, breadth))(index, box)
        .copy(
            attributes = attributes + (menuItemIndexKey to index)
        )
  }
      .addAttributes(menuKey to menu)
}

val faintBlack = black.copy(w = 0.6f)

fun menuFlower(title: Box, menu: Menu): StateFlower = { definitions, state ->
  dialogContent(title)(menuFlower(menu, state.menuFocusIndex, title.dimensions.x + 100))
}

fun menuFlower(title: Text, menu: Menu): StateFlower = { definitions, state ->
  menuFlower(dialogTitle(definitions.textLibrary(title)), menu)(definitions, state)
}

fun convertSimpleMenu(definitions: Definitions, source: List<SimpleMenuItem>): Menu =
    source.map {
      MenuItem(
          flower = menuTextFlower(definitions.textLibrary(it.text)),
          events = it.events
      )
    }

fun simpleMenuFlower(title: Text, source: List<SimpleMenuItem>): StateFlower = { definitions, state ->
  val menu = convertSimpleMenu(definitions, source)
  menuFlower(title, menu)(definitions, state)
}
