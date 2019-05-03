package marloth.clienting.gui

import haft.HaftCommands
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.isGuiActive
import mythic.bloom.*
import mythic.bloom.ButtonState
import mythic.bloom.next.Flower
import mythic.bloom.next.overlay
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.spatial.Vector4
import org.joml.Vector2i
import org.joml.Vector4i
import scenery.Text
import simulation.World

typealias TextResources = Map<Text, String>

enum class ViewId {
  mainMenu,
  none,
  victory
}

fun gameIsActive(world: World?): Boolean =
    world != null && world.gameOver == null

fun gameIsActiveByClient(state: ClientState): Boolean = !isGuiActive(state)

const val currentViewKey = "currentViewKey"
val currentView = existingOrNewState(currentViewKey) { ViewId.none }

const val menuCommandsKey = "menuCommands"
val menuCommands = existingOrNewState(menuCommandsKey) { listOf<GuiCommandType>() }

fun newBloomState() =
    BloomState(
        bag = mapOf(),
        input = InputState(
            mousePosition = Vector2i(),
            mouseButtons = listOf(ButtonState.up),
            events = listOf()
        )
    )

data class ButtonState(
    val text: String,
    val hasFocus: Boolean
)

fun depictBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

val menuBackground: Depiction = depictBackground(grayTone(0.5f))

fun haftToBloom(commands: HaftCommands<GuiCommandType>): List<BloomEvent> =
    commands.mapNotNull {
      when (it.type) {
        GuiCommandType.moveUp -> BloomEvent.up
        GuiCommandType.moveDown -> BloomEvent.down
        GuiCommandType.moveLeft -> BloomEvent.left
        GuiCommandType.moveRight -> BloomEvent.right
        GuiCommandType.menuBack -> BloomEvent.back
        GuiCommandType.menuSelect -> BloomEvent.activate
        else -> null
      }
    }

fun victoryMenu(): Menu = listOfNotNull(
    MenuOption(GuiCommandType.menu, Text.victory)
)

fun viewSelect(textResources: TextResources, world: World?, view: ViewId): Flower? {
  return when (view) {
    ViewId.mainMenu -> menuFlower(textResources, mainMenu(gameIsActive(world)))

    ViewId.victory -> menuFlower(textResources, victoryMenu())

    ViewId.none -> null
  }
}

fun guiLayout(client: Client, clientState: ClientState, world: World?, hudData: HudData?): Flower {
  val bloomState = clientState.bloomState
  return overlay(listOfNotNull(
      if (hudData != null) hudLayout(client.textResources, hudData) else null,
      viewSelect(client.textResources, world, clientState.view)
  ))
}

fun layoutGui(client: Client, clientState: ClientState, world: World?, hudData: HudData?, windowInfo: WindowInfo): FlatBoxes {
  val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
  val layout = convertFlower(guiLayout(client, clientState, world, hudData))
  val seed = SeedOld(
      bag = clientState.bloomState.bag,
      bounds = bounds
  )
  return layout(seed).boxes
}
