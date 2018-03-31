package lab

import commanding.CommandType
import lab.views.*
import marloth.clienting.Client
import mythic.drawing.Canvas
import mythic.drawing.getUnitScaling
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import scenery.GameScene
import simulation.AbstractWorld
import haft.*
import lab.views.model.ModelView
import org.joml.Vector2i
import org.joml.minus

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
      "game" -> GameView(config.gameView)
      "world" -> WorldView(config.worldView, abstractWorld, client.renderer)
      "model" -> ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())
      "texture" -> TextureView()
      else -> throw Error("Not supported")
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

  fun updateInput(view: View, layout: LabLayout, bindings: Bindings<LabCommandType>,
                  previousState: LabState, delta: Float): InputTriggerState<LabCommandType> {
    val (commands, nextLabInputState) = gatherInputCommands(bindings, deviceHandlers, previousState.labInput)
    applyCommands(commands, view.getCommands().plus(globalKeyPressCommands))
    val input = getInputState(client.platform.input, commands)
    view.updateState(layout, input, delta)
    return nextLabInputState
  }

  fun update(scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): ViewInputResult {
    val windowInfo = client.platform.display.getInfo()
    val view = selectView(config, metaWorld, client, config.view)
    client.renderer.prepareRender(windowInfo)
    val layout = view.createLayout(windowInfo.dimensions)
    client.platform.input.update()

    val bindings = labInputConfig["global"]!!.plus(labInputConfig[config.view]!!)
    val nextLabInputState = updateInput(view, layout, bindings, previousState, delta)

    if (config.view == "game") {
//      val (commands, nextLabInputState) = gatherInputCommands(labInputConfig["global"]!!, deviceHandlers, previousState.labInput)
//      applyCommands(commands, globalKeyPressCommands)
      val (gameCommands, nextGameInputState) = client.updateInput(previousState.gameInput, scenes.map { it.player })
//      val input = getInputState(client.platform.input, commands)
//      view.updateState(layout, input, delta)
      renderScene(client, GameViewRenderData(scenes, metaWorld, config.gameView))
      return Pair(gameCommands, LabState(nextLabInputState, nextGameInputState))
    } else {

    }
    renderLab(windowInfo, layout)
//    val bindings = labInputConfig["global"]!!.plus(labInputConfig[config.view]!!)
//    val (commands, nextLabInputState) = gatherInputCommands(bindings, deviceHandlers, previousState.labInput)
//    applyCommands(commands, view.getCommands().plus(globalKeyPressCommands))
//    val input = getInputState(client.platform.input, commands)
//    view.updateState(layout, input, delta)
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