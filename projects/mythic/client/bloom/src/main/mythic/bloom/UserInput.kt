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

fun updateStateBag(boxes: Boxes, state: HistoricalBloomState): StateBag =
    boxes.filter { it.logic != null }
//        .fold(state.bag) { bag, box -> box.logic!!(HistoricalBloomState(bag, state.input), box.bounds) }
        .flatMap { box -> box.logic!!(state, box.bounds).entries }
        .associate { it.toPair() }

fun updateBloomState(boxes: Boxes, previousState: BloomState, currentInput: InputState): BloomState {
  val historicalState = HistoricalBloomState(
      input = HistoricalInputState(
          previous = previousState.input,
          current = currentInput
      ),
      bag = previousState.bag
  )

  val newBag = updateStateBag(boxes, historicalState)

  return BloomState(
      input = currentInput,
      bag = newBag
  )
}

fun isClick(button: Int): (HistoricalInputState) -> Boolean = {
  it.previous.mouseButtons[button] == ButtonState.up
      && it.current.mouseButtons[button] == ButtonState.down
}

fun isClick() = isClick(0)

fun isClickInside(bounds: Bounds, inputState: HistoricalInputState) =
    isClick()(inputState) && isInBounds(inputState.current.mousePosition, bounds)