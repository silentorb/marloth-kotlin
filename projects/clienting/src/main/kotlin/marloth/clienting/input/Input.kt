package marloth.clienting.input

import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import silentorb.mythic.characters.rigs.MouseLookEvent
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.*
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.platforming.InputEvent
import silentorb.mythic.platforming.mouseDeviceIndex
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.toVector2

data class GameInputConfig(
    val placeholder: Boolean = true
)

enum class InputContext {
  game,
  menu
}

data class InputProfile(
    val bindings: Map<InputContext, Bindings>
)

const val defaultInputProfile: Long = 1L
typealias InputProfileId = Long

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val inputProfiles: Map<InputProfileId, InputProfile>,
    val playerProfiles: Map<Id, InputProfileId>,
    val deviceTypeMap: DeviceTypeMap,
    val devicePlayers: Map<Int, Id>
)

fun newInputState(config: GameInputConfig) =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        config = config,
        inputProfiles = mapOf(
            defaultInputProfile to InputProfile(
                bindings = mapOf(
                    InputContext.game to defaultGameInputBindings(),
                    InputContext.menu to defaultMenuInputProfile()
                )
            )
        ),
        playerProfiles = mapOf(),
        deviceTypeMap = mapOf(),
        devicePlayers = mapOf()
    )

fun bindingContext(playerViews: PlayerViews, player: Id): InputContext =
    if (playerViews[player] != null)
      InputContext.menu
    else
      InputContext.game

fun joiningGamepads(events: List<InputEvent>, deviceTypeMap: DeviceTypeMap): List<Int> {
  val currentDevices = deviceTypeMap.keys
  return events
      .filter { !currentDevices.contains(it.device) }
      .map { it.device }
      .distinct()
}

fun getInputProfile(inputState: InputState, player: Id): InputProfile? {
  val playerProfile = inputState.playerProfiles[player]
  return inputState.inputProfiles[playerProfile]
}

fun isStroke(context: InputContext, type: Any): Boolean =
    commandStrokes[context]!!.contains(type)

fun getBinding(inputState: InputState, playerViews: PlayerViews): BindingSource = { event ->
  val player = inputState.devicePlayers[event.device]
  val device = inputState.deviceTypeMap[event.device]
  if (player != null && device != null) {
    val profile = getInputProfile(inputState, player)
    if (profile != null) {
      val inputContext = bindingContext(playerViews, player)
      val strokes = commandStrokes[inputContext] ?: setOf()
      val binding = profile.bindings
          .getValue(inputContext)
          .firstOrNull { it.device == device && it.trigger == event.index }
      if (binding != null)
        Triple(binding, player, strokes.contains(binding.command))
      else
        null
    } else
      null
  } else
    null
}

fun isMouseDown(deviceStates: List<InputDeviceState>): Boolean =
    deviceStates.any { state -> state.events.any { it.device == mouseDeviceIndex && it.index == 0 } }

fun isMouseClickFinished(previous: List<InputDeviceState>, next: List<InputDeviceState>): Boolean =
    isMouseDown(previous) && !isMouseDown(next)

fun didMouseMove(previous: List<InputDeviceState>, next: List<InputDeviceState>): Boolean =
    previous.firstOrNull()?.mousePosition != next.firstOrNull()?.mousePosition

fun getMouseEvents(player: Id, previous: List<InputDeviceState>, next: List<InputDeviceState>) =
    listOfNotNull(
        if (isMouseClickFinished(previous, next))
          Command(type = GuiCommandType.mouseClick, device = mouseDeviceIndex, target = player)
        else
          null,
        if (didMouseMove(previous, next))
          Command(type = GuiCommandType.mouseMove, device = mouseDeviceIndex, target = player)
        else
          null,
    )

fun gatherInputCommands(previous: InputState, next: InputState, playerViews: PlayerViews): Commands {
  val getBinding = getBinding(next, playerViews)
  val deviceStates = next.deviceStates
  val commands = mapEventsToCommandsOld(deviceStates, getBinding)

  val firstPlayer = playerViews.keys.firstOrNull()
  val mouseCommands = if (firstPlayer != null)
    getMouseEvents(firstPlayer, previous.deviceStates, deviceStates)
  else
    listOf()

  return commands + mouseCommands
}

fun firstPlayer(clientState: ClientState) =
    clientState.players.firstOrNull()

fun isGameMouseActive(clientState: ClientState): Boolean =
    clientState.isEditorActive || getDebugBoolean("DISABLE_MOUSE") || clientState.guiStates[firstPlayer(clientState)]?.view != null

fun mouseLookEvents(dimensions: Vector2i, previousState: InputDeviceState?, nextState: InputDeviceState, character: Id?): Events =
    if (getDebugBoolean("DISABLE_MOUSE"))
      listOf()
    else {
      val previousMousePosition = previousState?.mousePosition ?: Vector2.zero
      val offset = nextState.mousePosition - previousMousePosition
      if (offset != Vector2.zero && character != null) {
        listOf(MouseLookEvent(
            character = character,
            offset = -offset / dimensions.toVector2()
        ))
      } else
        listOf()
    }
