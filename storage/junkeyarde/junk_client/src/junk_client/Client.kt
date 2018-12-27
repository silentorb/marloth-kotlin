package junk_client

import mythic.bloom.*
import mythic.platforming.Platform
import org.joml.Vector2i
import haft.*
import junk_client.views.ClientBattleState
import junk_client.views.shopView
import junk_client.views.battleView
import junk_simulation.CommandType
import junk_simulation.GameCommand
import mythic.platforming.WindowInfo
import mythic.spatial.Vector2
import mythic.spatial.minMax
import mythic.spatial.toVector2i
import org.lwjgl.glfw.GLFW

data class ClientState(
    val previousInput: InputTriggerState<CommandType>,
    val mode: GameMode,
    val shopState: ShopState?,
    val battle: ClientBattleState?
)

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  val bindings: Bindings<CommandType> = createStrokeBindings(1, mapOf(
      GLFW.GLFW_MOUSE_BUTTON_1 to CommandType.select
  ))

  fun getWindowInfo() = platform.display.getInfo()
  val deviceHandlers = createDeviceHandlers(platform.input)

  fun updateInputState(previousInput: InputTriggerState<CommandType>, actualWindowInfo: WindowInfo): Pair<
      UserInput, InputTriggerState<CommandType>> {
    platform.input.update()
    val (commands, nextInputState) = gatherInputCommands(bindings, deviceHandlers, previousInput)
    val initalMousePosition = platform.input.getMousePosition()
    val clippedMousePosition = Vector2(
        minMax(initalMousePosition.x, 0f, actualWindowInfo.dimensions.x.toFloat()),
        minMax(initalMousePosition.y, 0f, actualWindowInfo.dimensions.y.toFloat())
    )
    val mousePosition = Vector2(
        clippedMousePosition.x * 320 / actualWindowInfo.dimensions.x,
        clippedMousePosition.y * 200 / actualWindowInfo.dimensions.y
    )
    val userInput = UserInput(commands, mousePosition)
    return Pair(userInput, nextInputState)
  }

  fun getInputEvent(layout: LayoutOld, userInput: UserInput): Any? {
    val isActive = haft.isActive(userInput.commands)
    if (isActive(CommandType.select)) {
      val event = getEvent(layout, userInput.mousePosition.toVector2i())
      if (event != null)
        return event
    }

    return null
  }

  private fun prepareLayout(state: AppState, bounds: Bounds): LayoutOld {
    return when (state.client.mode) {
      GameMode.abilitySelection -> shopView(state.client.shopState!!, bounds)
      GameMode.battle -> battleView(state.client.battle!!, state.world!!, bounds)
    }
  }

  fun updateBattleState(state: ClientBattleState?, delta: Float): ClientBattleState? =
      if (state != null)
        state.copy(
            flicker = (state.flicker + 4f * delta) % 1f
        )
      else null

  fun update(state: AppState, delta: Float): Pair<ClientState, GameCommand?> {
    val actualWindowInfo = getWindowInfo()
    val windowInfo = actualWindowInfo.copy(dimensions = Vector2i(320, 200))
    val canvas = createCanvas(windowInfo)
    val bounds = Bounds(dimensions = windowInfo.dimensions)
    val (userInput, triggerState) = updateInputState(state.client.previousInput, actualWindowInfo)
    val layout = prepareLayout(state, bounds)
    renderScreen(renderer, layout, canvas, windowInfo, actualWindowInfo)
    val newClientState = state.client.copy(
        previousInput = triggerState,
        battle = updateBattleState(state.client.battle, delta)
    )
    val event = getInputEvent(layout, userInput)
    return if (event != null)
      applyInput(event, newClientState, state.world?.activeCreatureId)
    else
      Pair(newClientState, null)
  }
}