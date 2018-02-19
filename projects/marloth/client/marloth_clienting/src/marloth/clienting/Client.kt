package marloth.clienting

import commanding.*
import haft.*
import mythic.platforming.Platform
import rendering.Renderer
import scenery.Scene
import scenery.Screen

val maxPlayerCount = 4

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  val gamepadAssignments: MutableMap<Int, Int> = mutableMapOf()
  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
//      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
      CommandType.menu to { command -> platform.process.close() }
  )
  val playerInputProfiles = createDefaultInputProfiles()
  fun getWindowInfo() = platform.display.getInfo()

  fun updateInput(previousState: HaftInputState<CommandType>, players: List<Int>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val gamepads = platform.input.getGamepads().map { it.id }
    val waitingDevices = getWaitingDevices(gamepadAssignments, gamepads)
    val deviceHandlers = createDeviceHandlers(platform.input, gamepadAssignments, waitingDevices)
    val waitingGamepadProfiles = createWaitingGamepadProfiles(waitingDevices.size, gamepadAssignments.size)
    val profiles = selectActiveInputProfiles(playerInputProfiles, waitingGamepadProfiles, players)
    val (commands, nextState) = gatherProfileCommands(profiles, previousState.profileStates, deviceHandlers)
    handleKeystrokeCommands(commands, keyStrokeCommands)
    var playerCount = players.size
    val assignGamepad: (Command<CommandType>) -> Unit = { command ->
      gamepadAssignments[command.target - 10] = playerCount++
    }
    handleKeystrokeCommands(commands, mapOf(
        CommandType.activateDevice to assignGamepad,
        CommandType.joinGame to assignGamepad
    ))

    return Pair(commands.filterNot({ keyStrokeCommands.containsKey(it.type) }), HaftInputState(nextState))
  }

  fun update(scenes: List<Scene>, previousState: HaftInputState<CommandType>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val windowInfo = getWindowInfo()
    renderer.prepareRender(windowInfo)
    renderer.renderedScenes(scenes, windowInfo)
    return updateInput(previousState, scenes.map { it.player })
  }

}
