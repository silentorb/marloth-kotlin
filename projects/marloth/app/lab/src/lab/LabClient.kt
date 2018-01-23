package lab

import commanding.CommandType
import haft.*
import lab.views.LabLayout
import lab.views.createMapLayout
import lab.views.createTextureLayout
import lab.views.renderMainLab
import marloth.clienting.Client
import mythic.drawing.Canvas
import mythic.drawing.getUnitScaling
import mythic.platforming.WindowInfo
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import scenery.Scene
import simulation.MetaWorld

class LabClient(val config: LabConfig, val client: Client) {
  val keyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure },
      LabCommandType.toggleLab to { _ -> config.showLab = !config.showLab },
      LabCommandType.cycleView to { _ ->
        config.view = if (config.view == LabView.texture) LabView.world else LabView.texture
      }
  )
  val gameKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleLab to { _ -> config.showLab = !config.showLab }
  )
  val labInput = InputManager(config.input.bindings, client.deviceHandlers)

  fun update(scene: Scene, metaWorld: MetaWorld): Commands<CommandType> {
    val windowInfo = client.platform.display.getInfo()
    if (config.showLab) {
      val dimensions = Vector2(windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat())

      val labLayout = if (config.view == LabView.world)
        createMapLayout(metaWorld.abstractWorld, metaWorld.structureWorld, dimensions, config, client.renderer)
      else
        createTextureLayout(dimensions, config)

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
//    canvas.drawText(TextConfiguration(
//        "Dev Lab",
//        renderer.fonts[0],
//        12f,
//        Vector2(10f, 10f),
////        Vector4(1f, 0.8f, 0.3f, 1f)
//        Vector4(0f, 0f, 0f, 1f)
//    ))
    renderMainLab(labLayout, canvas)
  }

}