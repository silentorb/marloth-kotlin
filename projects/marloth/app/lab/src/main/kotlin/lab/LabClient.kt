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
import scenery.GameScene
import simulation.AbstractWorld
import haft.*
import lab.views.model.ModelView
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.getVerticesCenter
import org.joml.Vector2i
import org.joml.minus
import rendering.MeshType
import rendering.SceneRenderer

data class LabState(
    val labInput: InputTriggerState<LabCommandType>,
    val gameInput: HaftInputState<CommandType>
)

fun createLabDeviceHandlers(input: PlatformInput): List<ScalarInputSource> {
  val gamepad = input.getGamepads().firstOrNull()
  return listOf(
      input.KeyboardInputSource,
      input.MouseInputSource,
      if (gamepad != null)
        { trigger: Int -> input.GamepadInputSource(gamepad.id, trigger) }
      else
        disconnectedScalarInputSource
  )
}

fun selectView(config: LabConfig, abstractWorld: AbstractWorld, client: Client, view: String): View =
    when (view) {
      "game" -> GameView()
      "world" -> WorldView(config.worldView, abstractWorld, client.renderer)
      "model" -> ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())
      "texture" -> TextureView()
      else -> throw Error("Not supported")
    }


fun renderFaceNormals(renderer: SceneRenderer, mesh: FlexibleMesh, effects: Effects, modelTransform: Matrix = Matrix()) {
  globalState.lineThickness = 2f
  for (face in mesh.faces) {
    val faceCenter = getVerticesCenter(face.unorderedVertices)
    val transform = modelTransform
        .translate(faceCenter)
        .rotateTowards(face.normal, Vector3(0f, 0f, 1f))
        .rotateY(-Pi * 0.5f)

    effects.flat.activate(transform, Vector4(0f, 1f, 0f, 1f))
    renderer.meshes[MeshType.line]!![0].mesh.draw(DrawMethod.lines)
  }
}

private var previousMousePosition = Vector2i()

fun getInputState(platformInput: PlatformInput, commands: List<Command<LabCommandType>>): InputState {
  val mousePosition = platformInput.getMousePosition()
  val input = InputState(
      commands,
      mousePosition,
      mousePosition - previousMousePosition
  )
  previousMousePosition = mousePosition
  return input
}

class LabClient(val config: LabConfig, val client: Client) {

  val globalKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.viewGame to { _ -> config.view = "game" },
      LabCommandType.viewModel to { _ -> config.view = "model" },
      LabCommandType.viewWorld to { _ -> config.view = "world" },
      LabCommandType.viewTexture to { _ -> config.view = "texture" },
      LabCommandType.menu to { _ ->
        if (config.view != "game")
          client.platform.process.close()
      }
  )
  val deviceHandlers = createLabDeviceHandlers(client.platform.input)

  fun renderScene(scenes: List<GameScene>, metaWorld: AbstractWorld) {
    val windowInfo = client.getWindowInfo()
    val renderer = client.renderer
    renderer.renderScenes(scenes, windowInfo)
//    val effects = renderer.createEffects(scenes[0], windowInfo.dimensions)
//    renderFaceNormals(client.renderer, metaWorld.mesh, effects)
  }

  fun update(scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): ViewInputResult {
    val windowInfo = client.platform.display.getInfo()
    val view = selectView(config, metaWorld, client, config.view)
    client.renderer.prepareRender(windowInfo)
    val layout = view.createLayout(windowInfo.dimensions)
    client.platform.input.update()

    if (config.view == "game") {
      val (commands, nextLabInputState) = gatherInputCommands(labInputConfig["global"]!!, deviceHandlers, previousState.labInput)
      applyCommands(commands, globalKeyPressCommands)
      renderScene(scenes, metaWorld)
      val (gameCommands, nextGameInputState) = client.updateInput(previousState.gameInput, scenes.map { it.player })
      return Pair(gameCommands, LabState(nextLabInputState, nextGameInputState))
    } else {

    }
    renderLab(windowInfo, layout)
    val bindings = labInputConfig["global"]!!.plus(labInputConfig[config.view]!!)
    val (commands, nextLabInputState) = gatherInputCommands(bindings, deviceHandlers, previousState.labInput)
    applyCommands(commands, view.getCommands().plus(globalKeyPressCommands))
    val input = getInputState(client.platform.input, commands)
    view.updateState(layout, input, delta)
    return Pair(listOf(), LabState(nextLabInputState, previousState.gameInput))
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