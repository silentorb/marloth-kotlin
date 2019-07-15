package marloth.clienting.gui

import haft.HaftCommands
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.isGuiActive
import mythic.bloom.*
import mythic.bloom.ButtonState
import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.next.Seed
import mythic.bloom.next.compose
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.spatial.Vector4
import org.joml.Vector2i
import scenery.enums.Text
import simulation.happenings.GameEvent
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = Map<Text, String>

enum class ViewId {
  mainMenu,
  none,
  merchant,
  victory
}

val pauseViews = listOf(
    ViewId.mainMenu,
    ViewId.victory
)

fun gameIsActive(world: World?): Boolean =
    world != null && world.gameOver == null

fun gameIsActiveByClient(state: ClientState): Boolean = !isGuiActive(state)

enum class GuiEventType {
  command,
  gameEvent
}

data class GuiEvent(
    val type: GuiEventType,
    val data: Any
)

//const val currentViewKey = "currentViewKey"
//val currentView = existingOrNewState(currentViewKey) { ViewId.none }

//const val menuCommandsKey = "menuCommands"
//val menuCommands = existingOrNewState(menuCommandsKey) { listOf<GuiCommandType>() }

const val guiEventsKey = "guiEvents"
val guiEvents = existingOrNewState(guiEventsKey) { listOf<GuiEvent>() }

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

fun victoryMenu() = listOfNotNull(
    SimpleMenuItem(Text.message_victory, GuiCommandType.menu)
)

fun viewSelect(textResources: TextResources, definitions: Definitions, world: World?, view: ViewId): Flower? {
  return when (view) {
    ViewId.mainMenu -> menuFlower(textResources, mainMenu(gameIsActive(world)))
    ViewId.merchant -> merchantView(textResources, definitions.accessories, world!!.deck, 1L)
    ViewId.victory -> menuFlower(textResources, victoryMenu())
    ViewId.none -> null
  }
}

fun guiLayout(client: Client, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?): Flower {
  val bloomState = clientState.bloomState
  return compose(listOfNotNull(
      if (hudData != null) hudLayout(client.textResources, hudData) else null,
      viewSelect(client.textResources, definitions, world, clientState.view)
  ))
}

fun layoutGui(client: Client, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?, windowInfo: WindowInfo): Box {
  val layout = guiLayout(client, definitions, clientState, world, hudData)
  val seed = Seed(
      bag = clientState.bloomState.bag,
      dimensions = windowInfo.dimensions
  )
  return layout(seed)
}
