package marloth.clienting.menus

import marloth.clienting.ClientState
import marloth.clienting.getPlayerBloomState
import marloth.clienting.hud.hudLayout
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.Flower
import silentorb.mythic.bloom.next.Seed
import silentorb.mythic.bloom.next.compose
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.glowing.globalState
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = (Text) -> String?

enum class ViewId {
  characterInfo,
  chooseProfessionMenu,
  mainMenu,
  none,
  merchant,
  victory
}

//val pauseViews = listOf(
//    ViewId.mainMenu,
//    ViewId.victory
//)

fun gameIsActive(world: World?): Boolean =
    world != null && world.global.gameOver == null

enum class GuiEventType {
  command,
  gameEvent
}

data class GuiEvent(
    val type: GuiEventType,
    val data: Any
)

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
        CharacterCommands.moveUp -> BloomEvent.up
        CharacterCommands.moveDown -> BloomEvent.down
        CharacterCommands.moveLeft -> BloomEvent.left
        CharacterCommands.moveRight -> BloomEvent.right
        GuiCommandType.menuBack -> BloomEvent.back
        GuiCommandType.menuSelect -> BloomEvent.activate
        else -> null
      }
    }

fun victoryMenu() = listOfNotNull(
    SimpleMenuItem(Text.message_victory, command = GuiCommandType.newGame)
)

fun viewSelect(textResources: TextResources, definitions: Definitions, world: World?, view: ViewId, player: Id): Flower? {
  return when (view) {
    ViewId.characterInfo -> characterInfoViewOrChooseAbilityMenu(definitions, world!!.deck, player)
    ViewId.chooseProfessionMenu -> menuFlower(textResources, Text.gui_chooseProfessionMenu, chooseProfessionMenu(player))
    ViewId.mainMenu -> menuFlower(textResources, Text.gui_mainMenu, mainMenu(gameIsActive(world)))
    ViewId.merchant -> merchantView(textResources, definitions.accessories, world!!.deck, player)
    ViewId.victory -> menuFlower(textResources, Text.gui_victory, victoryMenu())
    ViewId.none -> null
  }
}

fun guiLayout(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?, player: Id): Flower {
  val view = clientState.playerViews[player] ?: ViewId.none
  return compose(listOfNotNull(
      if (world != null) hudLayout(textResources, world, player, view) else null,
      viewSelect(textResources, definitions, world, view, player)
  ))
}

fun layoutPlayerGui(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?,
                    dimensions: Vector2i, player: Id): Box {
  val layout = guiLayout(textResources, definitions, clientState, world, player)
  val bloomState = getPlayerBloomState(clientState.bloomStates, player)
  val seed = Seed(
      bag = bloomState.bag.plus(textResourcesKey to textResources),
      dimensions = dimensions
  )
  return layout(seed)
}
