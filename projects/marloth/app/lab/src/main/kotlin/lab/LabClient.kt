package lab

import commanding.CommandType
import lab.views.*
import marloth.clienting.Client
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import scenery.GameScene
import simulation.AbstractWorld
import haft.*
import lab.views.model.ModelView
import marloth.clienting.gui.MenuState
import marloth.clienting.gui.updateMenuState
import mythic.bloom.Layout
import mythic.bloom.renderLayout
import org.joml.Vector2i
import org.joml.minus
import rendering.createCanvas

data class LabState(
    val labInput: InputTriggerState<LabCommandType>,
    val gameInput: ProfileStates<CommandType>,
    val menuState: MenuState
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

  fun updateInput(view: View, layout: Layout, bindings: Bindings<LabCommandType>,
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
      val properties = client.prepareInput(previousState.gameInput, scenes.map { it.player })
      val gameResult = if (previousState.menuState.isVisible)
        client.updateGameInput(properties, client.menuInputProfiles)
      else
        client.updateGameInput(properties, client.playerInputProfiles)

      val waitingResult = client.checkForNewGamepads(properties)
      val (gameCommands, nextGameInputState) = gameResult + waitingResult
      val newMenuState = updateMenuState(previousState.menuState, filterKeystrokeCommands(gameCommands))
      renderScene(client, GameViewRenderData(scenes, metaWorld, config.gameView, previousState.menuState))
      return Pair(gameCommands, LabState(nextLabInputState, nextGameInputState, newMenuState))
    } else {
      renderLab(windowInfo, layout)
      return Pair(listOf(), LabState(nextLabInputState, previousState.gameInput, previousState.menuState))
    }
  }

  fun renderLab(windowInfo: WindowInfo, labLayout: Layout) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(labLayout, canvas)
  }

}