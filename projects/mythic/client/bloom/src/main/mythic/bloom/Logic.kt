package mythic.bloom

import org.joml.Vector2i

data class BloomState(
    val bag: StateBag,
    val input: InputState
)

data class HistoricalBloomState(
    val bag: StateBag,
    val input: HistoricalInputState
)

data class LogicBundle(
    val state: HistoricalBloomState,
    val bounds: Bounds,
    val visibleBounds: Bounds? // Null means the box is completely clipped and not visible
)

typealias LogicModule = (LogicBundle) -> StateBagMods

//data class LogicModule(
//    val key: String?,
//    val function: LogicModule
//)

fun visibleBounds(box: Box): Bounds? =
    if (box.clipBounds == null)
      box.bounds
    else {
      val b = box.bounds
      val c = box.clipBounds
      val clippedBounds = Bounds.fromEnds(
          Math.max(b.left, c.left),
          Math.max(b.top, c.top),
          Math.min(b.right, c.right),
          Math.min(b.bottom, c.bottom)
      )

      if (clippedBounds.dimensions.x <= 0f || clippedBounds.dimensions.y <= 0f) {
        null
      } else
        clippedBounds
    }

fun updateStateBag(boxes: Boxes, state: HistoricalBloomState): StateBag =
    boxes.filter { it.logic != null }
//        .fold(state.bag) { bag, box -> box.logic!!(HistoricalBloomState(bag, state.input), box.bounds) }
        .flatMap { box -> box.logic!!(LogicBundle(state, box.bounds, visibleBounds(box))).entries }
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

fun persist(key: String, logicModule: LogicModule): LogicModule = { bundle ->
  val visibleBounds = bundle.visibleBounds
  if (visibleBounds != null)
    logicModule(bundle)
  else {
    val flowerState = bundle.state.bag[key]
    if (flowerState != null)
      mapOf(key to flowerState)
    else
      mapOf()
  }
}

fun isInBounds(position: Vector2i, bounds: Bounds): Boolean =
    position.x >= bounds.position.x &&
        position.x < bounds.position.x + bounds.dimensions.x &&
        position.y >= bounds.position.y &&
        position.y < bounds.position.y + bounds.dimensions.y

fun logic(logicModule: LogicModule): Flower = { seed ->
  newBlossom(
      Box(
          bounds = seed.bounds,
          logic = logicModule
      )
  )
}

operator fun LogicModule.plus(b: LogicModule): LogicModule = { bundle ->
  this(bundle)
      .plus(b(bundle))
}
