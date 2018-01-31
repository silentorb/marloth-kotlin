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
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.sculpting.query.getCenter
import mythic.spatial.*
import rendering.Effects
import rendering.FlatColoredPerspectiveEffect
import scenery.Scene
import simulation.AbstractWorld

data class LabState(
    val labInput: ProfileStates<LabCommandType>,
    val gameInput: ProfileStates<CommandType>
)

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
//  val labInput = InputManager(config.input.bindings, client.deviceHandlers)

  fun renderFaceNormals(world: AbstractWorld, effects: Effects) {
    globalState.lineThickness = 2f
    for (face in world.mesh.faces) {
      val faceCenter = getCenter(face.vertices)
      val transform = Matrix()
          .translate(faceCenter)
          .rotateTowards(face.normal, Vector3(0f, 0f, 1f))
          .rotateY(-Pi * 0.5f)
//          .lookAlong(face.normal, Vector3(0f, 0f, 1f))

      effects.flat.activate(transform, Vector4(0f, 1f, 0f, 1f))
      client.renderer.meshes["line"]!!.draw(DrawMethod.lines)
    }
  }

  fun renderScene(scene: Scene, metaWorld: AbstractWorld) {
    val windowInfo = client.getWindowInfo()
    val renderer = client.renderer
    renderer.prepareRender(windowInfo)
    val effects = renderer.createEffects(scene, windowInfo)
    renderer.renderScene(scene, effects)
    renderFaceNormals(metaWorld, effects)
  }

  fun update(scene: Scene, metaWorld: AbstractWorld, previousState: LabState): Pair<Commands<CommandType>, LabState> {
    val windowInfo = client.platform.display.getInfo()
    if (config.showLab) {
      val dimensions = Vector2(windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat())

      val labLayout = if (config.view == LabView.world)
        createMapLayout(metaWorld, dimensions, config, client.renderer)
      else
        createTextureLayout(dimensions, config)

      client.renderer.prepareRender(windowInfo)
      renderLab(windowInfo, labLayout)
      val (commands, nextLabInputState) = gatherInputCommands(config.input.profiles, previousState.labInput, client.deviceHandlers)
      handleKeystrokeCommands(commands, keyPressCommands)
      return Pair(listOf(), LabState(nextLabInputState, mapOf()))
    } else {
      val (commands, nextLabInputState) = gatherInputCommands(config.input.profiles, previousState.labInput, client.deviceHandlers)
      handleKeystrokeCommands(commands, gameKeyPressCommands)
      renderScene(scene, metaWorld)
      val (gameCommands, nextGameInputState) = client.updateInput(previousState.gameInput)
      return Pair(gameCommands, LabState(nextLabInputState, nextGameInputState))
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