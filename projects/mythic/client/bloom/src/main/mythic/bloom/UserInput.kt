package mythic.bloom

import org.joml.Vector2i

enum class ButtonState {
  down,
  up
}

data class InputState(
    val mousePosition: Vector2i,
    val mouseButtons: List<ButtonState>
)

data class HistoricalInputState(
    val previous: InputState,
    val current: InputState
)

fun isClick(button: Int): (HistoricalInputState) -> Boolean = {
  it.previous.mouseButtons[button] == ButtonState.up
      && it.current.mouseButtons[button] == ButtonState.down
}

fun isClick() = isClick(0)

fun isClickInside(bounds: Bounds, inputState: HistoricalInputState) =
    isClick()(inputState) && isInBounds(inputState.current.mousePosition, bounds)

fun onClick(logicModule: LogicModule): LogicModule = { bundle ->
  val visibleBounds = bundle.visibleBounds
  if (visibleBounds != null && isClickInside(visibleBounds, bundle.state.input))
    logicModule(bundle)
  else {
    logicModule(bundle.copy(
        visibleBounds = null
    ))
  }
}