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
  var playerCount: Int = 1
  fun getWindowInfo() = platform.display.getInfo()

  fun updateInput(previousState: HaftInputState<CommandType>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val gamepadSlots = updateGamepadSlots(platform.input, previousState.gamepadSlots)
    val deviceHandlers: List<ScalarInputSource> = createDeviceHandlers(platform.input, previousState.gamepadSlots)
    val profiles = selectActiveInputProfiles(playerInputProfiles, playerCount)
    val (commands, nextState) = gatherInputCommands(profiles, previousState.profileStates, deviceHandlers)
    handleKeystrokeCommands(commands, keyStrokeCommands)
    return Pair(commands.filterNot({ keyStrokeCommands.containsKey(it.type) }), HaftInputState(nextState, gamepadSlots))
  }

  fun update(scene: Scene, previousState: HaftInputState<CommandType>):
      Pair<Commands<CommandType>, HaftInputState<CommandType>> {
    val windowInfo = getWindowInfo()
    renderer.prepareRender(windowInfo)
    renderer.renderScene(scene, windowInfo)
    return updateInput(previousState)
  }

}
