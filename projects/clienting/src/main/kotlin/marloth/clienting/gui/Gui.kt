package marloth.clienting.gui

import marloth.clienting.*
import marloth.clienting.gui.hud.overlayLayout
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import silentorb.mythic.bloom.menuItemIndexKey
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.logic.menuLengthKey
import marloth.clienting.gui.menus.views.options.commandKey
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.old.getAttributeValue
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.spatial.Vector2i
import simulation.main.World
import simulation.misc.Definitions

typealias TextResources = (Text) -> String?

enum class ViewId {
  audioOptions,
  characterContracts,
  characterInventory,
  characterStatus,
  chooseProfessionMenu,
  dev,
  displayChangeConfirmation,
  displayOptions,
  gamepadOptions,
  inputBindings,
  inputOptions,
  mainMenu,
  manual,
  conversation,
  conversationActiveContracts,
  conversationAvailableContracts,
  conversationMerchandise,
  mouseOptions,
  options,
  victory
}

data class BloomDefinition(
    val menuLength: Int?
)

fun newBloomDefinition(boxes: List<AttributeHolder>): BloomDefinition =
    BloomDefinition(
        menuLength = getAttributeValue<Int>(boxes, menuLengthKey)
    )

fun gameIsActive(world: World?): Boolean =
    world != null && world.global.gameOver == null

typealias EventUnion = Any

data class OnClientEvents(
    val map: List<Pair<EventUnion, (Command) -> Boolean>>
)

val menuBackground: Depiction = solidBackground(grayTone(0.5f))

fun victoryMenu() = listOfNotNull(
    newSimpleMenuItem(TextId.message_victory, ClientEvent(GuiCommandType.newGame))
)

val emptyViewFlower: StateFlower = { _, _ -> emptyFlower }

fun guiLayout(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?,
              player: Id): Flower {
  val state = clientState.guiStates[player]
  return compose(listOfNotNull(
      if (state != null) {
        val stateFlower = viewSelect(world, options, clientState, state.view, player)
        if (stateFlower != null)
          stateFlower(definitions, state)
        else
          null
      } else
        null
  ))
}

fun prepareBloomState(state: GuiState?) =
    (state?.bloomState ?: mapOf()) + mapOf(
        menuItemIndexKey to (state?.menuFocusIndex ?: defaultMenuFocusIndex)
    ) - commandKey

fun layoutPlayerGui(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, dimensions: Vector2i,
                    player: Id): Box {
  val layout = guiLayout(definitions, options, clientState, world, player)
  val state = clientState.guiStates[player]
  val seed = Seed(
      dimensions = dimensions,
      state = prepareBloomState(state),
      previousState = state?.bloomState ?: mapOf(),
  )
  return layout(seed)
}
