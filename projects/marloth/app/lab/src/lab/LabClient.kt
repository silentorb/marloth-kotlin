package lab

import commanding.CommandType
import haft.*
import marloth.clienting.Client
import marloth.clienting.switchCameraMode
import mythic.drawing.Canvas
import mythic.drawing.getUnitScaling
import mythic.platforming.Platform
import mythic.platforming.WindowInfo
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import rendering.convertMesh
import scenery.Scene

class LabClient(val config: LabConfig, val client: Client) {
  val keyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure },
      LabCommandType.toggleLab to { _ -> showLab = !showLab }

  )
  val gameKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleLab to { _ -> showLab = !showLab }
  )
  val labInput = InputManager(config.input.bindings, client.deviceHandlers)

  var showLab = true

  fun update(scene: Scene, marlothLab: MarlothLab): Commands<CommandType> {
    val windowInfo = client.platform.display.getInfo()
    if (showLab) {
      val dimensions = Vector2(windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat())

      val labLayout = createLabLayout(marlothLab.abstractWorld, marlothLab.structureWorld, dimensions,
          marlothLab.config)

      client.renderer.prepareRender(windowInfo)
      renderLab(windowInfo, labLayout)
      val commands = labInput.update()
      handleKeystrokeCommands(commands, keyPressCommands)
      return listOf()
    } else {
      val commands = labInput.update()
      handleKeystrokeCommands(commands, gameKeyPressCommands)
      return client.update(scene)
    }
  }

  fun renderLab(windowInfo: WindowInfo, labLayout: LabLayout) {
    val unitScaling = getUnitScaling(windowInfo.dimensions)
    val renderer = client.renderer
    val canvas = Canvas(renderer.vertexSchemas.drawing, renderer.canvasMeshes, renderer.shaders.drawing,
        unitScaling, windowInfo.dimensions)
    canvas.drawText(TextConfiguration(
        "Dev Lab",
        renderer.fonts[0],
        12f,
        Vector2(10f, 10f),
//        Vector4(1f, 0.8f, 0.3f, 1f)
        Vector4(0f, 0f, 0f, 1f)
    ))
    renderLab(labLayout, canvas)
  }

}