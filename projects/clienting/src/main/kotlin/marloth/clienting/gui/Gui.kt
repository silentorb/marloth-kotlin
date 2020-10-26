package marloth.clienting.gui

import marloth.clienting.*
import marloth.clienting.gui.hud.hudLayout
import marloth.clienting.gui.menus.*
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.logic.menuKey
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = (Text) -> String?

enum class ViewId {
  audioOptions,
  characterInfo,
  chooseProfessionMenu,
  displayChangeConfirmation,
  displayOptions,
  gamepadOptions,
  inputOptions,
  mainMenu,
  merchant,
  mouseOptions,
  options,
  victory
}

data class BloomDefinition(
    val menu: Menu?
)

fun newBloomDefinition(boxes: List<AttributeHolder>): BloomDefinition =
    BloomDefinition(
        menu = getAttributeValue<Menu>(boxes, menuKey)
    )

fun gameIsActive(world: World?): Boolean =
    world != null && world.global.gameOver == null

typealias EventUnion = Any

data class OnClientEvents(
    val map: List<Pair<EventUnion, (HaftCommand) -> Boolean>>
)

data class ButtonState(
    val text: String,
    val hasFocus: Boolean,
    val isEnabled: Boolean = true
)

val menuBackground: Depiction = solidBackground(grayTone(0.5f))

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
    newSimpleMenuItem(Text.message_victory, event = ClientEvent(GuiCommandType.newGame))
)

val emptyViewFlower: StateFlower = { _, _ -> emptyBox }

fun guiLayout(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, player: Id): Flower {
  val state = clientState.guiStates[player]
  return compose(listOfNotNull(
      if (world != null) hudLayout(definitions.textLibrary, world, clientState, player, state?.view) else null,
      if (state != null) {
        val stateFlower = viewSelect(world, options, clientState, state.view, player)
        if (stateFlower != null)
          dialogWrapperWithExtras(definitions, stateFlower(definitions, state))
        else
          null
      } else
        null
  ))
}

fun layoutPlayerGui(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, dimensions: Vector2i,
                    player: Id): Box {
  val layout = guiLayout(definitions, options, clientState, world, player)
  return layout(dimensions)
}
