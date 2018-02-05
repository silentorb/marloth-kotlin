package lab

import commanding.CommandType
import lab.views.*
import marloth.clienting.Client
import mythic.drawing.Canvas
import mythic.drawing.getUnitScaling
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import mythic.sculpting.query.getCenter
import mythic.spatial.*
import rendering.Effects
import scenery.Scene
import simulation.AbstractWorld
import haft.*

data class LabState(
    val labInput: ProfileStates<LabCommandType>,
    val gameInput: HaftInputState<CommandType>
)

fun createLabDeviceHandlers(input: PlatformInput): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource,
      disconnectedScalarInputSource
  )
}

fun createViews(config: LabConfig) = mapOf(
    "game" to GameView(),
    "map" to WorldView(config.worldView),
    "model" to ModelView(config.modelView),
    "texture" to TextureView()
)

class LabClient(val config: LabConfig, val client: Client) {
  val keyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure },
//      LabCommandType.toggleLab to { _ -> config.showLab = !config.showLab },
//      LabCommandType.cycleView to { _ ->
//        config.view = if (config.view == LabView.texture) LabView.world else LabView.texture
//      }
  )
  val gameKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleLab to { _ -> config.showLab = !config.showLab }
  )
  val deviceHandlers = createLabDeviceHandlers(client.platform.input)
  val views = createViews(config)
//  val labInput = InputManager(config.input.bindings, client.deviceHandlers)

  fun renderFaceNormals(world: AbstractWorld, effects: Effects) {
    globalState.lineThickness = 2f
    for (face in world.mesh.faces) {
      val faceCenter = getCenter(face.unorderedVertices)
      val transform = Matrix()
          .translate(faceCenter)
          .rotateTowards(face.normal, Vector3(0f, 0f, 1f))
          .rotateY(-Pi * 0.5f)
//          .lookAlong(face.normal, Vector3(0f, 0f, 1f))

      effects.flat.activate(transform, Vector4(0f, 1f, 0f, 1f))
      client.renderer.meshes["line"]!!.draw(DrawMethod.lines)
    }
  }

  fun renderScene(scenes: List<Scene>, metaWorld: AbstractWorld) {
    val windowInfo = client.getWindowInfo()
    val renderer = client.renderer
    renderer.prepareRender(windowInfo)
    renderer.renderedScenes(scenes, windowInfo)
    val effects = renderer.createEffects(scenes[0], windowInfo.dimensions)
    renderFaceNormals(metaWorld, effects)
  }

  fun update(scenes: List<Scene>, metaWorld: AbstractWorld, previousState: LabState): ViewInputResult {
    val windowInfo = client.platform.display.getInfo()
    val view = views[config.view]!!
    val layout = view.createLayout(windowInfo.dimensions)
    client.renderer.prepareRender(windowInfo)

    if (config.view == "game") {
      val (commands, nextLabInputState) = gatherInputCommands(config.input.profiles, previousState.labInput, deviceHandlers)
      handleKeystrokeCommands(commands, gameKeyPressCommands)
      renderScene(scenes, metaWorld)
      val (gameCommands, nextGameInputState) = client.updateInput(previousState.gameInput, scenes.map { it.player })
      return Pair(gameCommands, LabState(nextLabInputState, nextGameInputState))
    }
    else {

    }
    renderLab(windowInfo, layout)
    return view.input()
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