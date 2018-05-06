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
import marloth.clienting.gui.MenuActionType
import marloth.clienting.gui.MenuState
import marloth.clienting.gui.menuButtonAction
import marloth.clienting.gui.updateMenuState
import mythic.bloom.Box
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

//fun selectView(config: LabConfig, abstractWorld: AbstractWorld, client: Client, view: String): View =
//    when (view) {
//      "game" -> GameView(config.gameView)
//      "world" -> WorldView(config.worldView, abstractWorld, client.renderer)
//      "model" -> ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())
//      "texture" -> TextureView()
//      else -> throw Error("Not supported")
//    }

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
      LabCommandType.viewGame to { _ -> config.view = Views.game },
      LabCommandType.viewModel to { _ -> config.view = Views.model },
      LabCommandType.viewWorld to { _ -> config.view = Views.world },
      LabCommandType.viewTexture to { _ -> config.view = Views.texture }
//      LabCommandType.menu to { _ ->
//        if (config.view != "game")
//          client.platform.process.close()
//      }
  )
  val deviceHandlers = createLabDeviceHandlers(client.platform.input)

  fun updateInput(viewCommands: LabCommandMap, previousState: LabState): Pair<Commands<LabCommandType>,
      InputTriggerState<LabCommandType>> {
    val (commands, nextLabInputState) = gatherInputCommands(getBindings(), deviceHandlers, previousState.labInput)
    applyCommands(commands, viewCommands.plus(globalKeyPressCommands))
    return Pair(commands, nextLabInputState)
  }

  fun getBindings() = labInputConfig[Views.global]!!.plus(labInputConfig[config.view]!!)

  fun prepareClient(windowInfo: WindowInfo) {
    client.renderer.prepareRender(windowInfo)
    client.platform.input.update()
  }

  fun updateGame(windowInfo: WindowInfo, scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): LabClientResult {
    val view = GameView(config.gameView)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    val input = getInputState(client.platform.input, commands)
    view.updateState(input, delta)
    val properties = client.prepareInput(previousState.gameInput, scenes.map { it.player })
    val gameResult = if (previousState.menuState.isVisible)
      client.updateGameInput(properties, client.menuInputProfiles)
    else
      client.updateGameInput(properties, client.playerInputProfiles)

    val waitingResult = client.checkForNewGamepads(properties)
    val (gameCommands, nextGameInputState) = gameResult + waitingResult
    val menuCommands = filterKeystrokeCommands(gameCommands)
    val newMenuState = updateMenuState(previousState.menuState, menuCommands)
    val menuAction = menuButtonAction(newMenuState, menuCommands)
    client.handleMenuAction(menuAction)
    renderScene(client, GameViewRenderData(scenes, metaWorld, config.gameView, previousState.menuState))
    return LabClientResult(gameCommands, LabState(nextLabInputState, nextGameInputState, newMenuState), menuAction)
  }

  fun updateModel(windowInfo: WindowInfo, previousState: LabState, delta: Float): LabClientResult {
    val view = ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())

    val layout = view.createLayout(windowInfo.dimensions)
    val (commands, nextLabInputState) = updateInput(view.getCommands(), previousState)
    val input = getInputState(client.platform.input, commands)
    renderLab(windowInfo, layout.boxes)
    view.updateState(layout, input, delta)
    
    return LabClientResult(
        listOf(),
        LabState(nextLabInputState, previousState.gameInput, previousState.menuState),
        MenuActionType.none
    )
  }

  fun updateTexture(windowInfo: WindowInfo, previousState: LabState): LabClientResult {
    val view = TextureView()

    val layout = view.createLayout(windowInfo.dimensions)
    val (_, nextLabInputState) = updateInput(mapOf(), previousState)

    renderLab(windowInfo, layout)
    return LabClientResult(
        listOf(),
        LabState(nextLabInputState, previousState.gameInput, previousState.menuState),
        MenuActionType.none
    )
  }

  fun updateWorld(windowInfo: WindowInfo, metaWorld: AbstractWorld, previousState: LabState): LabClientResult {
    val view = WorldView(config.worldView, metaWorld, client.renderer)

    val layout = view.createLayout(windowInfo.dimensions)
    val (_, nextLabInputState) = updateInput(view.getCommands(), previousState)

    renderLab(windowInfo, layout)
    return LabClientResult(
        listOf(),
        LabState(nextLabInputState, previousState.gameInput, previousState.menuState),
        MenuActionType.none
    )
  }

  fun update(scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): LabClientResult {
    val windowInfo = client.platform.display.getInfo()
    prepareClient(windowInfo)
    return when (config.view) {
      Views.game -> updateGame(windowInfo, scenes, metaWorld, previousState, delta)
      Views.model -> updateModel(windowInfo, previousState, delta)
      Views.texture -> updateTexture(windowInfo, previousState)
      Views.world -> updateWorld(windowInfo, metaWorld, previousState)
      else -> throw Error("Not supported")
    }
  }

  fun renderLab(windowInfo: WindowInfo, boxes: List<Box>) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(boxes, canvas)
  }

}