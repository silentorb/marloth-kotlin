package lab

import silentorb.mythic.haft.*
import lab.views.LabClientResult
import lab.views.LabCommandMap
import lab.views.LabCommandState
import lab.views.TextureView
import lab.views.map.mapLayout
import lab.views.map.mapViewKeyStrokes
import lab.views.map.updateMapState
import lab.views.model.ModelView
import lab.views.model.ModelViewState
import marloth.clienting.Client
import marloth.clienting.clientBloomModules
import marloth.game.integration.AppState
import silentorb.mythic.bloom.globalMenuModule
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.Seed
import silentorb.mythic.bloom.next.emptyFlower
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.bloom.toAbsoluteBounds
import silentorb.mythic.bloom.updateBloomState
import silentorb.mythic.platforming.PlatformInput
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.bloom.input.newBloomInputState
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.lookinglass.createCanvas
import simulation.main.World
import silentorb.mythic.bloom.input.updateInputDeviceStates
import silentorb.mythic.lookinglass.prepareRender

data class LabState(
//    val labInput: InputTriggerState<LabCommandType>,
    val modelViewState: ModelViewState,
    val app: AppState
)

private var previousMousePosition = Vector2()

fun getInputState(platformInput: PlatformInput, commands: List<HaftCommand>): LabCommandState {
  val mousePosition = platformInput.getMousePosition()
  val input = LabCommandState(
      commands,
      mousePosition,
      mousePosition - previousMousePosition
  )
  previousMousePosition = mousePosition
  return input
}

val labLogicModules = clientBloomModules
    .plus(
        globalMenuModule
    )

class LabClient(val config: LabConfig, val client: Client) {

  val globalKeyPressCommands: Map<LabCommandType, CommandHandler> = mapOf(
      LabCommandType.viewGame to { _ -> config.view = Views.game },
      LabCommandType.viewMap to { _ -> config.view = Views.map },
      LabCommandType.viewModel to { _ -> config.view = Views.model }
  )

  val gameKeyPressCommands: Map<LabCommandType, CommandHandler> = mapOf(
      LabCommandType.toggleDrawPhysics to { _ -> config.gameView.drawPhysics = !config.gameView.drawPhysics }
  )

  fun getBindings() = labInputConfig[Views.global]!!.plus(labInputConfig[config.view]!!)

  fun updateInput(viewCommands: LabCommandMap, deviceStates: List<InputDeviceState>): HaftCommands {
    val bindings = getBindings()
    val commands = mapEventsToCommands(deviceStates, getBindingSimple(bindings, mapViewKeyStrokes))
    val moreCommands = viewCommands.plus(globalKeyPressCommands)
    val allCommands = if (config.view == Views.game)
      moreCommands.plus(gameKeyPressCommands)
    else
      moreCommands

    applyCommands(commands, allCommands)
    return commands
  }

  fun prepareClient(windowInfo: WindowInfo) {
    prepareRender(client.renderer, windowInfo)
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
    val newDeviceStates = updateInputDeviceStates(client.platform.input, state.app.client.input.deviceStates)
    val commands = updateInput(mapOf(), newDeviceStates)
    val layout = if (world != null) {
      mapLayout(client, world, world.deck, config.mapView)
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
    val (newBloomState, events) = updateBloomState(labLogicModules, box, state.app.client.bloomState, bloomInputState)

    for (e in events) {
      println(e.javaClass.name)
    }

    if (world != null) {
      val allCommands = commands
          .plus(events.filterIsInstance<LabCommandType>().map { HaftCommand(it) })
      val input = getInputState(client.platform.input, allCommands)
      updateMapState(config.mapView, world.realm, input, windowInfo, state.app.client.bloomState, delta)
    }
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

  fun update(world: World?, previousState: LabState, delta: Float): LabClientResult {
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
    val canvas = createCanvas(client.renderer, windowInfo.dimensions)
    renderLayout(box, canvas, true)
  }

}
