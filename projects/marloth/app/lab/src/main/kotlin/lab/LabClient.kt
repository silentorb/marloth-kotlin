package lab

import marloth.integration.AppState
import haft.*
import lab.views.*
import lab.views.map.mapLayout
import lab.views.map.updateMapState
import lab.views.model.ModelView
import lab.views.model.ModelViewState
import marloth.clienting.*
import marloth.clienting.input.newBloomInputState
import marloth.clienting.input.updateInputState
import mythic.bloom.*
import mythic.bloom.next.Box
import mythic.bloom.next.Seed
import mythic.bloom.next.emptyFlower

import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import mythic.spatial.Vector2
import org.joml.Vector2i
import rendering.createCanvas
import scenery.Screen
import simulation.misc.Realm
import simulation.main.World

data class LabState(
//    val labInput: InputTriggerState<LabCommandType>,
    val modelViewState: ModelViewState,
    val app: AppState
)

private var previousMousePosition = Vector2()

fun getInputState(platformInput: PlatformInput, commands: List<HaftCommand<LabCommandType>>): LabCommandState {
  val mousePosition = platformInput.getMousePosition()
  val input = LabCommandState(
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
      LabCommandType.viewMap to { _ -> config.view = Views.map },
      LabCommandType.viewModel to { _ -> config.view = Views.model }
  )

  val gameKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.toggleDrawPhysics to { _ -> config.gameView.drawPhysics = !config.gameView.drawPhysics }
  )

  fun getBindings() = labInputConfig[Views.global]!!.plus(labInputConfig[config.view]!!)

  fun updateInput(viewCommands: LabCommandMap, deviceStates: List<InputDeviceState>): HaftCommands<LabCommandType> {
    val bindings = getBindings()
    val commands = mapEventsToCommands(deviceStates, labCommandStrokes, haft.getBindingSimple(bindings))
    val moreCommands = viewCommands.plus(globalKeyPressCommands)
    val allCommands = if (config.view == Views.game)
      moreCommands.plus(gameKeyPressCommands)
    else
      moreCommands

    applyCommands(commands, allCommands)
    return commands
  }

  fun prepareClient(windowInfo: WindowInfo) {
    client.renderer.prepareRender(windowInfo)
  }

  fun updateModel(windowInfo: WindowInfo, previousState: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val view = ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())

//    val layout = view.createLayout(windowInfo.dimensions, previousState.modelViewState)
//    val (commands, nextLabInputState) = updateInput(view.getCommands(), previousState)
//    val input = getInputState(client.platform.input, commands)
//    renderLab(windowInfo, layout.boxes)
//    val modelViewState = view.updateState(layout, input, previousState.modelViewState, delta)
    view.release()

    return LabClientResult(
        listOf(),
        previousState//.copy(labInput = nextLabInputState, modelViewState = modelViewState)
    )
  }

  fun updateTexture(windowInfo: WindowInfo, previousState: LabState): LabClientResult {
    prepareClient(windowInfo)
    val view = TextureView()

//    val layout = view.createLayout(client.renderer, config.textureView, windowInfo.dimensions)
//    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
//    val input = getInputState(client.platform.input, commands)

//    renderLab(windowInfo, layout.boxes)
//    updateTextureState(layout, input, config.textureView, client.renderer)
    return LabClientResult(
        listOf(),
        previousState//.copy(labInput = nextLabInputState)
    )
  }

  fun updateMap(windowInfo: WindowInfo, world: World?, state: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val newDeviceStates = updateInputState(client.platform.input, state.app.client.input)
    val commands = updateInput(mapOf(), newDeviceStates)
    val layout = if (world != null) {
      val input = getInputState(client.platform.input, commands)
      updateMapState(config.mapView, world.realm, input, windowInfo, state.app.client.bloomState, delta)
      mapLayout(client, world.realm, world.deck, config.mapView)
    } else
      emptyFlower

    val seed = Seed(
        bag = state.app.client.bloomState.bag,
        dimensions = windowInfo.dimensions
    )
    val relativeBox = layout(seed)
    val box = toAbsoluteBounds(Vector2i(), relativeBox)
    renderLab(windowInfo, box)

    val newInputState = state.app.client.input.copy(
        deviceStates = newDeviceStates
    )

    val bloomInputState = newBloomInputState(newDeviceStates.last())
    val newBloomState = updateBloomState(box, state.app.client.bloomState, bloomInputState)

    return LabClientResult(
        listOf(),
        state.copy(
            app = state.app.copy(
                client = state.app.client.copy(
                    bloomState = newBloomState,
                    input = newInputState
                )
            )
        )
    )
  }

  fun update(world: World?, screens: List<Screen>, previousState: LabState, delta: Float): LabClientResult {
//    if (config.view != Views.game) {
    client.platform.input.isMouseVisible(true)
//    }

    val windowInfo = client.platform.display.getInfo()
    return when (config.view) {
//      Views.game -> updateGame(world, screens, previousState)
      Views.model -> updateModel(windowInfo, previousState, delta)
      Views.map -> updateMap(windowInfo, world, previousState, delta)
      else -> throw Error("Not supported")
    }
  }

  fun renderLab(windowInfo: WindowInfo, box: Box) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(box, canvas, true)
  }

}
