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