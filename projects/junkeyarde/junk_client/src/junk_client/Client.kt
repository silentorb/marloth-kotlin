package junk_client

import junk_simulation.AbilityType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.platforming.Platform
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i
import haft.*
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import org.lwjgl.glfw.GLFW

fun listItemDepiction(text: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(text, bounds.position, blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val centered = centeredPosition(bounds, textDimensions)
  val position = Vector2(bounds.position.x + 10f, centered.y)
  canvas.drawText(text, position, blackStyle)
}

private val itemHeight = 15f

fun abilitySelectionList(column: AbilitySelectionColumn, abilities: List<AbilityType>, bounds: Bounds): Layout {
  val padding = Vector2(0f, 5f)
  val rows = listBounds(verticalPlane, padding, bounds, abilities.map { itemHeight })
  return abilities.zip(rows, { a, b ->
    Box(
        bounds = b,
        depiction = listItemDepiction(a.name),
        handler = AbilitySelectionEvent(column, a)
    )
  })
}

fun abilitySelectionView(state: AbilitySelectionState, bounds: Bounds): Layout {
  val rowLengths = resolveLengths(bounds.dimensions.y, listOf(itemHeight, null, itemHeight))
  val rows = listBounds(verticalPlane, Vector2(), bounds, rowLengths)

  val columnBounds = splitBoundsHorizontal(rows[1])
  return abilitySelectionList(AbilitySelectionColumn.available, state.available, columnBounds.first)
      .plus(abilitySelectionList(AbilitySelectionColumn.selected, state.selected, columnBounds.second))
}

fun applyInput(event: Any, state: ClientState): ClientState =
    when (event.javaClass.kotlin) {

      AbilitySelectionEvent::class -> {
        state.copy(
            abilitySelectionState = handleAbilitySelectionEvent(
                event as AbilitySelectionEvent, state.abilitySelectionState!!
            )
        )
      }

      else -> throw Error("Unsupported event type.")
    }

enum class CommandType {
  select
}

private fun createDeviceHandlers(input: PlatformInput): List<ScalarInputSource> {
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

data class UserInput(
    val commands: Commands<CommandType>,
    val mousePosition: Vector2i
)

data class ClientState(
    val previousInput: InputTriggerState<CommandType>,
    val mode: GameMode,
    val abilitySelectionState: AbilitySelectionState?
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
    val mousePosition = Vector2i(
        initalMousePosition.x * 320 / actualWindowInfo.dimensions.x,
        initalMousePosition.y * 200 / actualWindowInfo.dimensions.y
    )
    val userInput = UserInput(commands, mousePosition)
    return Pair(userInput, nextInputState)
  }

  fun updateInput(layout: Layout, state: ClientState, userInput: UserInput): ClientState {
    val isActive = haft.isActive(userInput.commands)
    if (isActive(CommandType.select)) {
      val event = getEvent(layout, userInput.mousePosition)
      if (event != null)
        return applyInput(event, state)
    }

    return state
  }

  fun update(state: AppState, delta: Float): ClientState {
    val actualWindowInfo = getWindowInfo()
    val windowInfo = actualWindowInfo.copy(dimensions = Vector2i(320, 200))
    val canvas = createCanvas(windowInfo)
    val bounds = Bounds(dimensions = windowInfo.dimensions.toVector2())
    val layout = abilitySelectionView(state.client.abilitySelectionState!!, bounds)
    renderScreen(renderer, layout, canvas, windowInfo, getWindowInfo())
    val (userInput, triggerState) = updateInputState(state.client.previousInput, actualWindowInfo)
    return updateInput(layout, state.client.copy(previousInput = triggerState), userInput)
  }
}
