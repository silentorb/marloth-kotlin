package marloth.clienting

import commanding.*
import haft.*
import mythic.platforming.Platform
import rendering.Renderer
import scenery.CameraMode
import scenery.Scene
import scenery.Screen

fun switchCameraMode(playerId: Int, screens: List<Screen>) {
  val currentMode = screens[playerId].cameraMode
  screens[playerId].cameraMode =
      if (currentMode == CameraMode.topDown)
        CameraMode.thirdPerson
      else
        CameraMode.topDown
}

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  //  val config: Configuration = createNewConfiguration(gamepads)
  val screens: List<Screen> = listOf(Screen(CameraMode.topDown, 1))
  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
      CommandType.menuBack to { command -> platform.process.close() }
  )
  val playerInputProfiles = createDefaultInputProfiles()
  val waitingGamepadProfiles = createWaitingGamepadProfiles()
  fun getWindowInfo() = platform.display.getInfo()

  fun updateInput(previousState: HaftInputState<CommandType>, players: List<Int>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val playerSlots = (1..maxGamepadCount).map { players.contains(it) }
    val gamepadSlots = updateGamepadSlots(platform.input, previousState.gamepadSlots)
    val deviceHandlers = createDeviceHandlers(platform.input, gamepadSlots, playerSlots)
    val profiles = selectActiveInputProfiles(playerInputProfiles, waitingGamepadProfiles, players)
    val (commands, nextState) = gatherInputCommands(profiles, previousState.profileStates, deviceHandlers)
    handleKeystrokeCommands(commands, keyStrokeCommands)
    return Pair(commands.filterNot({ keyStrokeCommands.containsKey(it.type) }), HaftInputState(nextState, gamepadSlots))
  }

  fun update(scenes: List<Scene>, previousState: HaftInputState<CommandType>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val windowInfo = getWindowInfo()
    renderer.prepareRender(windowInfo)
    renderer.renderScene(scenes[0], windowInfo)
    return updateInput(previousState, scenes.map { it.player })
  }

}
