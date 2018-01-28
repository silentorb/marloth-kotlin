package marloth.clienting

import commanding.*
import haft.*
import mythic.glowing.SimpleMesh
import mythic.platforming.Platform
import rendering.Renderer
import rendering.convertMesh
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
  val config: Configuration = createNewConfiguration()
  val deviceHandlers = createDeviceHandlers(platform.input)
  val screens: List<Screen> = listOf(Screen(CameraMode.topDown, 0))
  var userInput = InputManager(config.input.bindings, deviceHandlers)
  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
      CommandType.switchView to { command -> switchCameraMode(command.target, screens) }
  )

  fun getWindowInfo() = platform.display.getInfo()

  fun updateInput(): Commands<CommandType> {
    val commands = userInput.update()
    handleKeystrokeCommands(commands, keyStrokeCommands)
    return commands.filterNot({ keyStrokeCommands.containsKey(it.type) })
  }

  fun update(scene: Scene): Commands<CommandType> {
    val windowInfo = getWindowInfo()
    renderer.prepareRender(windowInfo)
    renderer.renderScene(scene, windowInfo)
    return updateInput()
  }

}
