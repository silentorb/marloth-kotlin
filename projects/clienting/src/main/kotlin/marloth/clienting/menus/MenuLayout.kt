package marloth.clienting.menus

import marloth.clienting.StateFlower
import marloth.clienting.hud.versionDisplay
import marloth.clienting.input.GuiCommandType
import marloth.clienting.resources.UiTextures
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.TextConfiguration
import silentorb.mythic.typography.calculateTextDimensions
import silentorb.mythic.typography.resolveTextStyle
import simulation.misc.Definitions

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

fun drawMenuButtonFront(state: ButtonState, bounds: Bounds, canvas: Canvas) {
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

fun drawMenuButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  drawMenuButtonFront(state, bounds, canvas)
}

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

val embeddedMenuBox: (Menu) -> FlowerWrapper = { menu ->
  div(
      reverse = shrink
  )
}

fun menuFlowerBase(menuBox: (Menu) -> FlowerWrapper): (Menu, Int) -> Flower = { menu, focusIndex ->
  val rows = menu
      .mapIndexed { index, item ->
        val hasFocus = index == focusIndex
        val flower = menuButton(item.flower, hasFocus)
        val event = item.event
        val attributes = if (hasFocus)
          mapOf(onActivateKey to event, onClickKey to event)
        else
          mapOf()

        withAttributes(attributes + (menuItemIndexKey to index))(
            flower
        )
      }

  val gap = 20

  menuBox(menu)(
      margin(all = gap)(
          list(verticalPlane, gap)(rows)
      )
  )
}

val menuFlower = menuFlowerBase(embeddedMenuBox)

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
                          menuFlower(menu, state.menuFocusIndex)
                      )
                  )
              )
          )
      )
  )
}
