package marloth.clienting.input

import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import marloth.clienting.editing.EditingMode
import silentorb.mythic.characters.rigs.mouseLookEvent
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.*
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.platforming.Devices
import silentorb.mythic.platforming.InputEvent
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformInput
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.toVector2

fun defaultInputProfiles() =
    mapOf(
        defaultInputProfile to InputProfile(
            bindings = defaultInputBindings(),
        )
    )

data class InputOptions(
    val profiles: Map<InputProfileId, InputProfile> = defaultInputProfiles(),
)

enum class InputContext {
  game,
  interaction,
  menu
}

data class InputProfile(
    val bindings: Map<InputContext, Bindings>
)

const val defaultInputProfile: Long = 1L
typealias InputProfileId = Long

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val playerProfiles: Map<Id, InputProfileId>,
    val deviceTypeMap: DeviceTypeMap,
    val devicePlayers: Map<Int, Id>
)

fun defaultInputBindings() = mapOf(
    InputContext.game to defaultGameInputBindings(),
    InputContext.interaction to defaultInteractionInputProfile(),
    InputContext.menu to defaultMenuInputProfile()
)

fun newInputState(input: PlatformInput) =
    InputState(
        deviceStates = listOf(newInputDeviceState(input)),
        playerProfiles = mapOf(),
        deviceTypeMap = mapOf(),
        devicePlayers = mapOf()
    )

fun joiningGamepads(events: List<InputEvent>, deviceTypeMap: DeviceTypeMap): List<Int> {
  val currentDevices = deviceTypeMap.keys
  return events
      .filter { !currentDevices.contains(it.device) }
      .map { it.device }
      .distinct()
}

fun getInputProfile(options: InputOptions, inputState: InputState, player: Id): InputProfile? {
  val playerProfile = inputState.playerProfiles[player] ?: defaultInputProfile
  return options.profiles[playerProfile]
}

fun isStroke(context: InputContext, type: Any): Boolean =
    commandStrokes[context]!!.contains(type)

fun isMouseDown(deviceStates: List<InputDeviceState>): Boolean =
    deviceStates.any { state -> state.events.any { it.device == Devices.mouse && it.index == 0 } }

fun isMouseClickFinished(previous: List<InputDeviceState>, next: List<InputDeviceState>): Boolean =
    isMouseDown(previous) && !isMouseDown(next)

fun didMouseMove(previous: List<InputDeviceState>, next: List<InputDeviceState>): Boolean =
    previous.firstOrNull()?.mousePosition != next.firstOrNull()?.mousePosition

fun getMouseEvents(player: Id, previous: List<InputDeviceState>, next: List<InputDeviceState>) =
    listOfNotNull(
        if (isMouseClickFinished(previous, next))
          Command(type = HaftCommand.leftMouseClick, target = player, device = Devices.mouse)
        else
          null,
        if (didMouseMove(previous, next))
          Command(type = GuiCommandType.mouseMove, target = player, device = Devices.mouse)
        else
          null,
    )

fun gatherInputCommandsWithoutPlayers(deviceStates: List<InputDeviceState>): Commands {
  val bindings = defaultGameInputBindings()
  val strokes = commandStrokes[InputContext.menu] ?: setOf()
  return mapInputToCommands(strokes, bindings, deviceStates)
}

fun bindingContext(playerViews: PlayerViews, player: Id): InputContext =
    when {
      playerViews[player] != null -> InputContext.menu
      else -> InputContext.game
    }

fun getBindings(bindings: Map<InputContext, Bindings>, playerViews: PlayerViews, interactions: Collection<Id>, player: Id): Bindings? =
    when {
      interactions.contains(player) -> {
        val original = bindings[InputContext.game] ?: listOf()
        val overrides = bindings[InputContext.interaction] ?: listOf()
        original
            .filter { binding -> overrides.none { it.device == binding.device && it.index == binding.index } }
            .plus(overrides)
      }
      playerViews[player] != null -> bindings[InputContext.menu]
      else -> bindings[InputContext.game]
    }

fun gatherInputCommandsForPlayers(options: InputOptions, inputState: InputState, playerViews: PlayerViews,
                                  interactions: Collection<Id>): Commands =
    playerViews
        .flatMap { (player, _) ->
          val profile = getInputProfile(options, inputState, player)
          if (profile == null)
            listOf()
          else {
            val inputContext = bindingContext(playerViews, player)
            val strokes = commandStrokes[inputContext] ?: setOf()
            val bindings = getBindings(profile.bindings, playerViews, interactions, player) ?: listOf()
            mapInputToCommands(strokes, bindings, inputState.deviceStates)
                .map { command ->
                  command.copy(
                      target = player
                  )
                }
          }
        }

fun gatherInputCommands(options: InputOptions, previous: InputState, next: InputState, playerViews: PlayerViews,
                        interactions: Collection<Id>): Commands {
  val deviceStates = next.deviceStates
  val commands = if (playerViews.none())
    gatherInputCommandsWithoutPlayers(deviceStates)
  else
    gatherInputCommandsForPlayers(options, next, playerViews, interactions)

  val firstPlayer = playerViews.keys.firstOrNull()
  val mouseCommands = if (firstPlayer != null)
    getMouseEvents(firstPlayer, previous.deviceStates, deviceStates)
  else
    listOf()

  return commands + mouseCommands
}

fun firstPlayer(clientState: ClientState) =
    clientState.players.firstOrNull()

fun isGameMouseActive(platform: Platform, clientState: ClientState): Boolean =
    clientState.editingMode != EditingMode.none ||
        getDebugBoolean("DISABLE_MOUSE") ||
        clientState.guiStates[firstPlayer(clientState)]?.view != null ||
        !platform.display.hasFocus()

fun mouseLookEvents(dimensions: Vector2i, previousState: InputDeviceState?, nextState: InputDeviceState, character: Id?): Events =
    if (getDebugBoolean("DISABLE_MOUSE"))
      listOf()
    else {
      val previousMousePosition = previousState?.mousePosition ?: Vector2.zero
      val offset = nextState.mousePosition - previousMousePosition
      if (offset != Vector2.zero && character != null) {
        listOf(Command(
            type = mouseLookEvent,
            target = character,
            value = -offset / dimensions.toVector2()
        ))
      } else
        listOf()
    }
