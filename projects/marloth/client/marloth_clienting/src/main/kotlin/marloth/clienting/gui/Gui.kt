package marloth.clienting.gui

import haft.HaftCommands
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.isAnyGuiActive
import mythic.bloom.*
import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.next.Seed
import mythic.bloom.next.compose
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.ent.Id
import mythic.glowing.globalState
import mythic.spatial.Vector4
import org.joml.Vector2i
import scenery.enums.Text
import simulation.input.CommandType
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = Map<Text, String>

enum class ViewId {
  characterInfo,
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

fun gameIsActiveByClient(state: ClientState): Boolean = !isAnyGuiActive(state)

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
//val menuCommands = existingOrNewState(menuCommandsKey) { listOf() }

const val guiEventsKey = "guiEvents"
val guiEvents = existingOrNewState(guiEventsKey) { listOf<GuiEvent>() }

data class ButtonState(
    val text: String,
    val hasFocus: Boolean,
    val isEnabled: Boolean = true
)

fun depictBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

val menuBackground: Depiction = depictBackground(grayTone(0.5f))

fun haftToBloom(commands: HaftCommands): List<BloomEvent> =
    commands.mapNotNull {
      when (it.type) {
        CommandType.moveUp -> BloomEvent.up
        CommandType.moveDown -> BloomEvent.down
        CommandType.moveLeft -> BloomEvent.left
        CommandType.moveRight -> BloomEvent.right
        GuiCommandType.menuBack -> BloomEvent.back
        GuiCommandType.menuSelect -> BloomEvent.activate
        else -> null
      }
    }

fun victoryMenu() = listOfNotNull(
    SimpleMenuItem(Text.message_victory, GuiCommandType.newGame)
)

fun viewSelect(textResources: TextResources, definitions: Definitions, world: World?, view: ViewId, player: Id): Flower? {
  return when (view) {
    ViewId.characterInfo -> characterInfoView(definitions, world!!.deck, player)
    ViewId.mainMenu -> menuFlower(textResources, Text.gui_mainMenu, mainMenu(gameIsActive(world)))
    ViewId.merchant -> merchantView(textResources, definitions.accessories, world!!.deck, player)
    ViewId.victory -> menuFlower(textResources, Text.gui_victory, victoryMenu())
    ViewId.none -> null
  }
}

fun guiLayout(client: Client, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?, player: Id): Flower {
  return compose(listOfNotNull(
      if (hudData != null) hudLayout(client.textResources, hudData) else null,
      viewSelect(client.textResources, definitions, world, clientState.playerViews[player] ?:ViewId.none, player)
  ))
}

fun layoutPlayerGui(client: Client, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?, dimensions: Vector2i, player: Id): Box {
  val layout = guiLayout(client, definitions, clientState, world, hudData, player)
  val seed = Seed(
      bag = clientState.bloomState.bag.plus(textResourcesKey to client.textResources),
      dimensions = dimensions
  )
  return layout(seed)
}
