package clienting

import commanding.Command
import commanding.CommandLifetime
import commanding.CommandType
import commanding.Commands
import haft.createDeviceHandlers
import haft.createEmptyInputState
import haft.gatherCommands
import haft.getCurrentInputState
import lab.createLabLayout
import mythic.spatial.Vector2
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.system.MemoryStack
import rendering.Renderer
import rendering.WindowInfo
import scenery.CameraMode
import scenery.Scene
import scenery.Screen

fun getWindowInfo(window: Long): WindowInfo {
  MemoryStack.stackPush().use { stack ->
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    glfwGetWindowSize(window, width, height)

    return WindowInfo(Vector2i(width.get(), height.get()))
  }
}

fun switchCameraMode(playerId: Int, screens: List<Screen>) {
  val currentMode = screens[playerId].cameraMode
  screens[playerId].cameraMode =
      if (currentMode == CameraMode.topDown)
        CameraMode.thirdPerson
      else
        CameraMode.topDown
}

typealias CommandHandler = (Command) -> Unit

class Client(val window: Long) {
  private val renderer: Renderer = Renderer(window)
  private val config: Configuration = createNewConfiguration()
  private val deviceHandlers = createDeviceHandlers(window)
  val screens: List<Screen> = listOf(Screen(CameraMode.topDown, 0))
  var inputState = createEmptyInputState(config.input.bindings)
  val keyPressCommands: Map<CommandType, CommandHandler> = mapOf(
      CommandType.switchView to { command -> switchCameraMode(command.target, screens) }
  )

  fun update(scene: Scene): Commands {
    val windowInfo = getWindowInfo(window)
    val labLayout = createLabLayout(Vector2(windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat()))
    renderer.render(scene, windowInfo, labLayout)
    inputState = getCurrentInputState(config.input.bindings, deviceHandlers, inputState)
    val commands = gatherCommands(inputState)
    commands.filter({ keyPressCommands.containsKey(it.type) && it.lifetime == CommandLifetime.end })
        .forEach({ keyPressCommands[it.type]!!(it) })

    return commands.filterNot({ keyPressCommands.containsKey(it.type) })
  }

}