package marloth.clienting.menus

import marloth.clienting.input.GuiCommandType
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.TextConfiguration
import silentorb.mythic.typography.calculateTextDimensions
import silentorb.mythic.typography.resolveTextStyle
import silentorb.mythic.spatial.Vector2i
import marloth.scenery.enums.Text
import kotlin.math.min

typealias MenuItemFlower = (Boolean) -> Flower

data class MenuItem(
    val flower: MenuItemFlower,
    val event: GuiEvent?
)

data class SimpleMenuItem(
    val text: Text,
    val event: GuiEvent? = null,
    val command: GuiCommandType? = null
)

typealias Menu = List<MenuItem>

fun cycle(value: Int, max: Int) = (value + max) % max

fun menuFocusIndexLogic(menu: Menu): LogicModuleOld = { bundle ->
  val events = bundle.state.input.current.events
  val index = menuFocusIndex(menu.size, bundle.state.bag)
  val newIndex = when {
    events.contains(BloomEvent.down) -> cycle(index + 1, menu.size)
    events.contains(BloomEvent.up) -> cycle(index - 1, menu.size)
    else -> index
  }
  mapOf(menuFocusIndexKey to newIndex)
}

fun addEvent(bag: StateBag, event: GuiEvent?): StateBagMods {
  return if (event == null)
    mapOf()
  else {
    val events = guiEvents(bag)

    mapOf(
        guiEventsKey to events.plus(event)
    )
  }
}

fun eventLogic(handler: (LogicBundle) -> GuiEvent?): LogicModuleOld = { bundle ->
  val bag = bundle.state.bag
  addEvent(bag, handler(bundle))
}

val menuNavigationLogic: LogicModuleOld = { bundle ->
  mapOf()
//  val events = bundle.state.input.current.events
//  val bag = bundle.state.bag
//  val activated = events.contains(BloomEvent.activate)
//  val newEvent = if (activated || events.contains(BloomEvent.back))
//    GuiEvent(GuiEventType.command, GuiCommandType.menuBack)
//  else
//    null
//
//  addEvent(bag, newEvent)
}

fun menuCommandLogic(menu: Menu): LogicModuleOld = eventLogic { bundle ->
  if (menu.none()) {
    null
  } else {
    val inputEvents = bundle.state.input.current.events
    val bag = bundle.state.bag
    val activated = inputEvents.contains(BloomEvent.activate)
    val index = menuFocusIndex(menu.size, bag)
    val menuItem = menu[index]
    if (activated)
      menuItem.event
    else
      null
  }
}

fun drawMenuButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (state.hasFocus)
    Pair(textStyles.mediumBlack, LineStyle(Vector4(1f), 2f))
  else
    Pair(textStyles.mediumBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), resolveTextStyle(canvas.fonts, style.first))
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, style.first, state.text)
}

const val menuFocusIndexKey = "menuFocusIndex"

fun menuFocusIndex(menuSize: Int, bag: StateBag): Int =
    min((bag[menuFocusIndexKey] ?: 0) as Int, menuSize - 1)

fun menuLogic(menu: Menu): LogicModuleOld =
    menuFocusIndexLogic(menu) combineLogic menuNavigationLogic combineLogic menuCommandLogic(menu)

private val buttonDimensions = Vector2i(200, 50)

fun menuButton(flower: MenuItemFlower, menuSize: Int, index: Int): Flower = { seed: Seed ->
  val hasFocus = menuFocusIndex(menuSize, seed.bag) == index
  flower(hasFocus)(seed)
}

fun simpleMenuButton(content: String): MenuItemFlower = { hasFocus ->
  { seed: Seed ->
    Box(
        bounds = Bounds(
            dimensions = buttonDimensions
        ),
        depiction = drawMenuButton(
            ButtonState(content, hasFocus)
        ),
        name = "simple menu button"
    )
  }
}

val standaloneMenuBox: (Menu) -> FlowerWrapper = { menu ->
  div(
      reverse = centerDialog,
      depiction = menuBackground,
      logic = menuLogic(menu)
  )
}

val embeddedMenuBox: (Menu) -> FlowerWrapper = { menu ->
  div(
      reverse = shrink,
      logic = menuLogic(menu)
  )
}

fun menuFlowerBase(menuBox: (Menu) -> FlowerWrapper): (Menu) -> Flower = { menu ->
  val rows = menu
      .mapIndexed { index, it ->
        //        val content = textResources[it.text]!!
        menuButton(it.flower, menu.size, index)
      }

  val gap = 20

  menuBox(menu)(
      margin(all = gap)(
          list(verticalPlane, gap)(rows)
      )
  )
}

val menuFlower = menuFlowerBase(standaloneMenuBox)

val embeddedMenuFlower = menuFlowerBase(embeddedMenuBox)

fun menuFlower(textResources: TextResources, title: Text, menu: List<SimpleMenuItem>): Flower {
  val items = menu.map {
    MenuItem(
        flower = simpleMenuButton(textResources(it.text)!!),
        event = it.event ?: GuiEvent(GuiEventType.command, it.command!!)
    )
  }
  return dialog(title)(
      embeddedMenuFlower(items)
  )
}
