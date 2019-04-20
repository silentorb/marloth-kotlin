package mythic.bloom

import mythic.spatial.*
import org.joml.Vector2i

private const val scrollbarWidth = 15

fun scrollbar(offset: Int, contentLength: Int): Depiction = { b, c ->
  if (contentLength > b.dimensions.y) {
    val width = scrollbarWidth
    val bounds = Bounds(
        x = b.end.x - width - 2,
        y = b.top + offset * b.dimensions.y / contentLength,
        width = width,
        height = b.dimensions.y * b.dimensions.y / contentLength
    )

    val viewport = bounds.toVector4i().toVector4()

    c.drawSquare(viewport.xy(), viewport.zw, c.solid(Vector4(0.6f, 0.6f, 0.6f, 1f)))
  }
}

fun clipBox(clipBounds: Bounds): (FlatBox) -> FlatBox = { box ->
  val depiction = if (box.depiction != null)
    clipBox(clipBounds, box.depiction)
  else
    null

  box.copy(
      depiction = depiction,
      clipBounds = clipBounds
  )
}

data class ScrollingState(
    val dragOrigin: Vector2i?,
    val offsetOrigin: Int,
    val offset: Int
)

val scrollingState = existingOrNewState {
  ScrollingState(
      dragOrigin = null,
      offsetOrigin = 0,
      offset = 0
  )
}

//fun extractOffset(key: String, input: (Vector2i) -> Flower): Flower = { seed ->
//  val state = scrollingState(seed.bag[key])
//  input(Vector2i(0, -state.withOffset))(seed)
//}

fun extractOffset(key: String, bag: StateBag): Vector2i {
  val state = scrollingState(bag[key])
  return Vector2i(0, -state.offset)
}

fun scrollingInteraction(key: String, contentBounds: Bounds): LogicModule = { (bloomState, bounds) ->
  if (contentBounds.dimensions.y <= bounds.dimensions.y) {
    mapOf()
  } else {
    val state = scrollingState(bloomState.bag[key])
    val input = bloomState.input
    val currentButton = input.current.mouseButtons[0]
    val previousButton = input.previous.mouseButtons[0]
    val clip = minMax(0, contentBounds.dimensions.y - bounds.dimensions.y)

    val (dragOrigin, offsetOrigin) = if (currentButton == ButtonState.down && previousButton == ButtonState.up
        && isInBounds(input.current.mousePosition, bounds))
      Pair(input.current.mousePosition, state.offset)
    else if (currentButton == ButtonState.up)
      Pair(null, state.offset) // Reclip the bounds in case the layout was changed independent of this code
    else
      Pair(state.dragOrigin, state.offsetOrigin)

    val offset = if (dragOrigin != null) {
      val mouseOffsetY = input.current.mousePosition.y - dragOrigin.y
      val mod = offsetOrigin + mouseOffsetY * contentBounds.dimensions.y / bounds.dimensions.y
//    println(mod)
      clip(mod)
    } else
      state.offset

    val newState = ScrollingState(
        dragOrigin = dragOrigin,
        offsetOrigin = offsetOrigin,
        offset = clip(offset)
    )
    mapOf(key to newState)
  }
}

fun scrollBox(key: String, contentBounds: Bounds): FlowerOld = { seed ->
  newBlossom(
      FlatBox(
          bounds = seed.bounds,
          depiction = scrollbar(scrollingState(seed.bag[key]).offset, contentBounds.dimensions.y),
          logic = scrollingInteraction(key, contentBounds)
      )
  )
}

fun scrolling(key: String): (FlowerOld) -> FlowerOld = { child ->
  { seed ->
    val innerSeed = seed.copy(
        bounds = seed.bounds.copy(
            dimensions = Vector2i(
                seed.bounds.dimensions.x - scrollbarWidth,
                seed.bounds.dimensions.y
            )
        )
    )
    val blossom = withOffset(child)(extractOffset(key, seed.bag))(innerSeed)
    val childBoxes = blossom.boxes
    if (childBoxes.any()) {
      val contentBounds = accumulatedBounds(childBoxes)
      scrollBox(key, contentBounds)(seed)
          .plus(childBoxes)
    } else {
      emptyBlossom
    }
  }
}