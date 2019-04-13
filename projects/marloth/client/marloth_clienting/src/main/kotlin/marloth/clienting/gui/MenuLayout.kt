package marloth.clienting.gui

import marloth.clienting.input.GuiCommandType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i
import scenery.Text

data class MenuOption(
    val command: GuiCommandType,
    val text: Text
)

typealias Menu = List<MenuOption>

fun cycle(value: Int, max: Int) = (value + max) % max

fun menuFocusIndexLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val index = menuFocusIndex(bundle.state.bag)
  val newIndex = when {
    events.contains(BloomEvent.down) -> cycle(index + 1, menu.size)
    events.contains(BloomEvent.up) -> cycle(index - 1, menu.size)
    else -> index
  }
  mapOf(menuFocusIndexKey to newIndex)
}

fun menuNavigationLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val bag = bundle.state.bag
  val view = currentView(bag)
  val activated = events.contains(BloomEvent.activate)
  val newView = if (activated || events.contains(BloomEvent.back))
    ViewId.none
  else
    view

  mapOf(
      currentViewKey to newView
  )
}

fun menuCommandLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val bag = bundle.state.bag
  val activated = events.contains(BloomEvent.activate)
  val commands = if (activated)
    listOf(menu[menuFocusIndex(bag)].command)
  else
    listOf()

  mapOf(
      menuCommandsKey to commands
  )
}

fun drawMenuButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (state.hasFocus)
    Pair(12f, LineStyle(Vector4(1f), 2f))
  else
    Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, blackStyle, state.text)
}

const val menuFocusIndexKey = "menuFocusIndex"

fun menuFocusIndex(bag: StateBag): Int =
    (bag[menuFocusIndexKey] ?: 0) as Int

fun menuLogic(menu: Menu): LogicModule =
    menuFocusIndexLogic(menu) + menuNavigationLogic(menu) + menuCommandLogic(menu)

fun menuFlower(textResources: TextResources, menu: Menu): Flower = { seed ->
  val buttonHeight = 50
  val items = menu
      .mapIndexed { index, it ->
        val content = textResources[it.text]!!
        PartialBox(buttonHeight, drawMenuButton(
            ButtonState(content, menuFocusIndex(seed.bag) == index)
        ))
      }

  val itemLengths = items.map { it.length }
  val menuHeight = listContentLength(10, itemLengths)
  val menuBounds = centeredBounds(seed.bounds, Vector2i(200, menuHeight))
  val menuPadding = 10

  listOf(Box(
      bounds = menuBounds,
      depiction = menuBackground,
      logic = menuLogic(menu)
  ))
      .plus(arrangeListComplex(lengthArranger(vertical, menuPadding), items, menuBounds))
}
