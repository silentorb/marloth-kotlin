package marloth.clienting

import commanding.*
import haft.*
import mythic.platforming.Input
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

fun createDeviceHandlers(input: Input, gamepads: List<Gamepad>): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource
  ).plus(gamepads.map { gamepad ->
    { trigger: Int -> input.GamepadInputSource(gamepad.id, trigger) }
  })
}

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  val gamepads = platform.input.getGamepads()
  val config: Configuration = createNewConfiguration(gamepads)
  val deviceHandlers = createDeviceHandlers(platform.input, gamepads)
  val screens: List<Screen> = listOf(Screen(CameraMode.topDown, 0))
  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
      CommandType.menuBack to { command -> platform.process.close() }
  )

  fun getWindowInfo() = platform.display.getInfo()

  fun updateInput(previousState: ProfileStates<CommandType>): InputProfileResult<CommandType> {
    val (commands, nextState) = gatherInputCommands(config.input.profiles, previousState, deviceHandlers)
    handleKeystrokeCommands(commands, keyStrokeCommands)
    return Pair(commands.filterNot({ keyStrokeCommands.containsKey(it.type) }), nextState)
  }

  fun update(scene: Scene, previousState: ProfileStates<CommandType>): InputProfileResult<CommandType> {
    val windowInfo = getWindowInfo()
    renderer.prepareRender(windowInfo)
    renderer.renderScene(scene, windowInfo)
    return updateInput(previousState)
  }

}
