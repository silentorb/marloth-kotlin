package marloth.clienting.menus

import marloth.clienting.StateFlower
import marloth.clienting.hud.versionDisplay
import marloth.clienting.input.GuiCommandType
import marloth.clienting.resources.UiTextures
import marloth.scenery.enums.CharacterRigCommands
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.glowing.globalState
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.TextConfiguration
import silentorb.mythic.typography.calculateTextDimensions
import silentorb.mythic.typography.resolveTextStyle
import simulation.misc.Definitions
import kotlin.math.min

const val menuFocusIndexKey = "silentorb.menuFocusIndex"
const val menuKey = "silentorb.menu"

fun addEvent(bag: StateBag, event: ClientOrServerEvent?): StateBag {
  return if (event == null)
    mapOf()
  else {
    val events = guiEvents(bag)

    mapOf(
        guiEventsKey to events.plus(event)
    )
  }
}

typealias MenuItemFlower = (Boolean) -> Flower

data class MenuItem(
    val flower: MenuItemFlower,
    val event: ClientOrServerEvent?
)

data class SimpleMenuItem(
    val text: Text,
    val event: ClientOrServerEvent? = null,
    val command: GuiCommandType? = null
)

typealias Menu = List<MenuItem>

fun cycle(value: Int, max: Int) = (value + max) % max

data class MenuLayer(
    val view: ViewId,
    val focusIndex: Int
)

typealias MenuStack = List<MenuLayer>

//fun menuCommandLogic(menu: Menu, bag: StateBag, events: List<BloomEvent>): StateBag {
//  val activated = events.contains(BloomEvent.activate)
//  val index = menuFocusIndex(menu.size, bag)
//  val menuItem = menu[index]
//  return addEvent(bag,
//      if (activated)
//        menuItem.event
//      else
//        null
//  )
//}

fun emitMenuEvents(menu: Menu?, index: Int, events: HaftCommands): List<ClientOrServerEvent> =
    if (menu == null)
      listOf()
    else {
      val activated = events.any { it.type == GuiCommandType.menuSelect }
      val menuItem = menu[index]
      if (activated)
        listOfNotNull(menuItem.event)
      else
        listOf()
    }

fun updateMenuFocus(stack: MenuStack, menuSize: Int, command: HaftCommand?, index: Int) =
    when (command?.type) {
      CharacterRigCommands.moveDown -> cycle(index + 1, menuSize)
      CharacterRigCommands.moveUp -> cycle(index - 1, menuSize)
      GuiCommandType.navigate -> 0
      GuiCommandType.menuBack -> stack.lastOrNull()?.focusIndex ?: 0
      else -> index
    }

//fun menuLogic(menu: Menu): LogicModuleOld = { bundle ->
//  val bag = bundle.state.bag
//  val events = bundle.state.input.current.events
//  menuCommandLogic(menu, bag, events)
//}

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

fun getMenuFocusIndex(bag: StateBag): Int =
    getBagEntry(bag, menuFocusIndexKey) { 0 }

fun menuFocusIndex(menuSize: Int, bag: StateBag): Int =
    min(getMenuFocusIndex(bag), menuSize - 1)

private val buttonDimensions = Vector2i(200, 50)

fun menuButton(flower: MenuItemFlower, hasFocus: Boolean): Flower = { seed: Seed ->
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
      depiction = menuBackground
  )
}

val embeddedMenuBox: (Menu) -> FlowerWrapper = { menu ->
  div(
      reverse = shrink
  )
}

fun menuFlowerBase(menuBox: (Menu) -> FlowerWrapper): (Menu, Int) -> Flower = { menu, focusIndex ->
  val rows = menu
      .mapIndexed { index, it ->
        //        val content = textResources[it.text]!!
        menuButton(it.flower, index == focusIndex)
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

val faintBlack = black.copy(w = 0.6f)

fun menuFlower(definitions: Definitions, title: Text, source: List<SimpleMenuItem>): StateFlower = { state ->
  val menu = source.map {
    MenuItem(
        flower = simpleMenuButton(definitions.textLibrary(it.text)),
        event = it.event ?: clientEvent(it.command!!)
    )
  }
  compose(
      div(forward = stretchBoth)(
          depict(solidBackground(faintBlack))
      ),
      versionDisplay(definitions.applicationInfo.version),
      div(reverse = centerDialog, data = mapOf(menuKey to menu))(
          reversePair(verticalPlane, 20)(
              Pair(
                  div(reverse = reverseOffset(left = centered), forward = forwardDimensions(fixed(500), fixed(90)))(
                      imageElement(UiTextures.marlothTitle)
                  ),
                  div(reverse = shrink, depiction = menuBackground)(
                      dialogContent(title)(
                          embeddedMenuFlower(menu, state.menuFocusIndex)
                      )
                  )
              )
          )
      )
  )
}
