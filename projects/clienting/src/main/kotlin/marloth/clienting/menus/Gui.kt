package marloth.clienting.menus

import silentorb.mythic.haft.HaftCommands
import marloth.clienting.ClientState
import marloth.clienting.hud.HudData
import marloth.clienting.hud.hudLayout
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.Flower
import silentorb.mythic.bloom.next.Seed
import silentorb.mythic.bloom.next.compose
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.Vector2i
import marloth.scenery.enums.Text
import silentorb.mythic.happenings.CommonCharacterCommands
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

//val pauseViews = listOf(
//    ViewId.mainMenu,
//    ViewId.victory
//)

fun gameIsActive(world: World?): Boolean =
    world != null && world.gameOver == null

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
        CommonCharacterCommands.moveUp -> BloomEvent.up
        CommonCharacterCommands.moveDown -> BloomEvent.down
        CommonCharacterCommands.moveLeft -> BloomEvent.left
        CommonCharacterCommands.moveRight -> BloomEvent.right
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

fun guiLayout(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?, player: Id): Flower {
  return compose(listOfNotNull(
      if (hudData != null) hudLayout(textResources, hudData) else null,
      viewSelect(textResources, definitions, world, clientState.playerViews[player] ?: ViewId.none, player)
  ))
}

fun layoutPlayerGui(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?, hudData: HudData?, dimensions: Vector2i, player: Id): Box {
  val layout = guiLayout(textResources, definitions, clientState, world, hudData, player)
  val seed = Seed(
      bag = clientState.bloomState.bag.plus(textResourcesKey to textResources),
      dimensions = dimensions
  )
  return layout(seed)
}
