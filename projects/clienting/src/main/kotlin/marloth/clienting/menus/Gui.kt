package marloth.clienting.menus

import marloth.clienting.ClientState
import marloth.clienting.MarlothBloomState
import marloth.clienting.StateFlower
import marloth.clienting.getPlayerBloomState
import marloth.clienting.hud.hudLayout
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.BloomEvent
import silentorb.mythic.bloom.Depiction
import silentorb.mythic.bloom.existingOrNewState
import silentorb.mythic.bloom.next.*
import silentorb.mythic.bloom.solidBackground
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

fun newBloomDefinition(data: Map<String, Any>): BloomDefinition =
    BloomDefinition(
        menu = getBagEntry(data, menuKey) { null }
    )

fun gameIsActive(world: World?): Boolean =
    world != null && world.global.gameOver == null

enum class GuiEventDomain {
  client,
  server
}

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

const val guiEventsKey = "guiEvents"
val guiEvents = existingOrNewState(guiEventsKey) { listOf<ClientOrServerEvent>() }

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

val emptyViewFlower: StateFlower = { emptyFlower }

fun viewSelect(textResources: TextResources, definitions: Definitions, world: World?, state: MarlothBloomState, player: Id): StateFlower? {
  return when (state.view) {
    ViewId.audioOptions -> emptyViewFlower
    ViewId.displayOptions -> emptyViewFlower
    ViewId.gamepadOptions -> emptyViewFlower
    ViewId.inputOptions -> inputOptionsMenu(definitions)
    ViewId.mouseOptions -> emptyViewFlower
    ViewId.options -> optionsMenu(definitions)
    ViewId.characterInfo -> characterInfoViewOrChooseAbilityMenu(definitions, world!!.deck, player)
    ViewId.chooseProfessionMenu -> menuFlower(definitions, Text.gui_chooseProfessionMenu, chooseProfessionMenu(player))
    ViewId.mainMenu -> mainMenu(definitions, world)
    ViewId.merchant -> merchantView(textResources, definitions.accessories, world!!.deck, player)
    ViewId.victory -> menuFlower(definitions, Text.gui_victory, victoryMenu())
    null -> null
  }
}

fun guiLayout(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?, player: Id): Flower {
  val state = clientState.bloomStates[player]
  return compose(listOfNotNull(
      if (world != null) hudLayout(textResources, world, player, state?.view) else null,
      if (state != null) viewSelect(textResources, definitions, world, state, player)?.invoke(state) else null
  ))
}

fun layoutPlayerGui(textResources: TextResources, definitions: Definitions, clientState: ClientState, world: World?,
                    dimensions: Vector2i, player: Id): Box {
  val layout = guiLayout(textResources, definitions, clientState, world, player)
  val bloomState = getPlayerBloomState(clientState.bloomStates, player)
  val seed = Seed(
      bag = bloomState.bloom.resourceBag.plus(textResourcesKey to textResources),
      dimensions = dimensions
  )
  return layout(seed)
}
