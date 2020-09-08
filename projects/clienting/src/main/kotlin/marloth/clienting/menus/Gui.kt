package marloth.clienting.menus

import marloth.clienting.*
import marloth.clienting.hud.hudLayout
import marloth.clienting.input.GuiCommandType
import marloth.clienting.menus.views.*
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector2i
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = (Text) -> String?

enum class ViewId {
  audioOptions,
  characterInfo,
  chooseProfessionMenu,
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

fun newBloomDefinition(boxes: List<Box>): BloomDefinition =
    BloomDefinition(
        menu = getAttributeValue<Menu>(boxes, menuKey)
    )

fun gameIsActive(world: World?): Boolean =
    world != null && world.global.gameOver == null

data class GuiEvent(
    val type: GuiCommandType,
    val data: Any? = null
)

data class ClientOrServerEvent(
    val client: GuiEvent? = null,
    val server: GameEvent? = null
) {
  init {
    assert((client == null && server != null) || (client != null && server == null))
  }
}

fun clientEvent(type: GuiCommandType, data: Any? = null): ClientOrServerEvent =
    ClientOrServerEvent(client = GuiEvent(type, data))

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
    SimpleMenuItem(Text.message_victory, command = GuiCommandType.newGame)
)

val emptyViewFlower: StateFlower = { _, _ -> emptyFlower }

fun viewSelect(world: World?, options: AppOptions, view: ViewId?, player: Id): StateFlower? {
  return when (view) {
    ViewId.audioOptions -> emptyViewFlower
    ViewId.displayOptions -> displayOptionsFlower(options.display)
    ViewId.gamepadOptions -> emptyViewFlower
    ViewId.inputOptions -> inputOptionsMenu
    ViewId.mouseOptions -> emptyViewFlower
    ViewId.options -> optionsMenu
    ViewId.characterInfo -> characterInfoViewOrChooseAbilityMenu(world!!.deck, player)
    ViewId.chooseProfessionMenu -> simpleMenuFlower(Text.gui_chooseProfessionMenu, chooseProfessionMenu(player))
    ViewId.mainMenu -> mainMenu(world)
    ViewId.merchant -> merchantView(world!!.deck, player)
    ViewId.victory -> simpleMenuFlower(Text.gui_victory, victoryMenu())
    null -> null
  }
}

fun guiLayout(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, player: Id): Flower {
  val state = clientState.bloomStates[player]
  return compose(listOfNotNull(
      if (world != null) hudLayout(definitions.textLibrary, world, player, state?.view) else null,
      if (state != null) viewSelect(world, options, state.view, player)?.invoke(definitions, state) else null
  ))
}

fun layoutPlayerGui(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, dimensions: Vector2i,
                    player: Id): Box {
  val layout = guiLayout(definitions, options, clientState, world, player)
  val bloomState = getPlayerBloomState(clientState.bloomStates, player)
  val seed = Seed(
      bag = bloomState.bloom.resourceBag.plus(textResourcesKey to definitions.textLibrary),
      dimensions = dimensions
  )
  return layout(seed)
}
