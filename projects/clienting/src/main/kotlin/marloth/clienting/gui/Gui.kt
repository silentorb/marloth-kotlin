package marloth.clienting.gui

import marloth.clienting.*
import marloth.clienting.gui.hud.hudLayout
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.Menu
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.menus.logic.menuKey
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
  displayChangeConfirmation,
  displayOptions,
  gamepadOptions,
  inputOptions,
  mainMenu,
  messageTooSoon,
  conversation,
  conversationActiveContracts,
  conversationAvailableContracts,
  conversationMerchandise,
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
    val map: List<Pair<EventUnion, (Command) -> Boolean>>
)

val menuBackground: Depiction = solidBackground(grayTone(0.5f))

fun victoryMenu() = listOfNotNull(
    newSimpleMenuItem(TextId.message_victory, event = ClientEvent(GuiCommandType.newGame))
)

val emptyViewFlower: StateFlowerTransform = { _, _ -> emptyFlower }

fun guiLayout(definitions: Definitions, options: AppOptions, clientState: ClientState, world: World?, player: Id): Flower {
  val state = clientState.guiStates[player]
  return compose(listOfNotNull(
      if (world != null) hudLayout(definitions.textLibrary, world, clientState, player, state?.view) else null,
      if (state != null) {
        val stateFlower = viewSelect(world, options, clientState, state.view, player)
        if (stateFlower != null)
          stateFlower(definitions, state)
//          dialogWrapperWithExtras(definitions, stateFlower(definitions, state))
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
