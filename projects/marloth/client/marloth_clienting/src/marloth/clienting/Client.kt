package marloth.clienting

import commanding.*
import haft.createDeviceHandlers
import haft.createEmptyInputState
import haft.gatherCommands
import haft.getCurrentInputState
import lab.MarlothLab
import lab.createLabInputMap
import lab.createLabLayout
import mythic.platforming.Platform
import mythic.spatial.Vector2
import mythic.spatial.Vector4
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
  private val renderer: Renderer = Renderer()
  private val config: Configuration = createNewConfiguration()
  private val deviceHandlers = createDeviceHandlers(platform.input)
  var showLab = true
  val marlothLab: MarlothLab? = MarlothLab()
  val screens: List<Screen> = listOf(Screen(CameraMode.topDown, 0))
  var inputState = createEmptyInputState(config.input.bindings)
  val keyPressCommands: Map<CommandType, CommandHandler> = mapOf<CommandType, CommandHandler>(
      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
      CommandType.toggleLab to { _ -> showLab = !showLab }
  ).plus(createLabInputMap(marlothLab!!.config))

  init {
    renderer.worldMesh = convertMesh(marlothLab!!.structureWorld.mesh, renderer.vertexSchemas.standard,
        Vector4(0.5f, 0.2f, 0f, 1f))
  }

  fun update(scene: Scene): Commands {
    val windowInfo = platform.display.getInfo()
    val dimensions = Vector2(windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat())

    renderer.prepareRender(windowInfo)

    if (showLab) {
      val labLayout = createLabLayout(marlothLab!!.abstractWorld, marlothLab.structureWorld, dimensions,
          marlothLab.config)
      renderer.renderLab(windowInfo, labLayout)
    } else {
      renderer.renderScene(scene, windowInfo)
    }
    inputState = getCurrentInputState(config.input.bindings, deviceHandlers, inputState)
    val commands = gatherCommands(inputState)
    commands.filter({ keyPressCommands.containsKey(it.type) && it.lifetime == CommandLifetime.end })
        .forEach({ keyPressCommands[it.type]!!(it) })

    return commands.filterNot({ keyPressCommands.containsKey(it.type) })
  }

}