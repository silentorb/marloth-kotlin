package mythic.bloom

import mythic.spatial.*
import org.joml.Vector2i
import org.joml.plus

fun scrollbar(offset: Int, contentLength: Int): Depiction = { b, c ->
  val width = 15
  val bounds = Bounds(
      x = b.end.x - width - 2,
      y = offset * b.dimensions.y / contentLength,
      width = width,
      height = b.dimensions.y * b.dimensions.y / contentLength
  )

  val viewport = bounds.toVector4i().toVector4()

  c.drawSquare(viewport.xy(), viewport.zw, c.solid(Vector4(0.6f, 0.6f, 0.6f, 1f)))
}

fun clipChildren(child: Flower): Flower = { b ->
  child(b).map { box ->
    val depiction = if (box.depiction != null)
      clip(b.bounds, box.depiction)
    else
      null

    box.copy(
        depiction = depiction
    )
  }
}

fun offset(flower: Flower): (Vector2i) -> Flower = { value ->
  { flower(Seed(it.bag, Bounds(it.bounds.position + value, it.bounds.dimensions))) }
}

data class ScrollingState(
    val dragOrigin: Vector2i?,
    val offsetOrigin: Int,
    val offset: Int
)

fun scrollingState(state: Any?): ScrollingState =
    if (state != null)
      state as ScrollingState
    else
      ScrollingState(
          dragOrigin = null,
          offsetOrigin = 0,
          offset = 0
      )

fun extractOffset(key: String, input: (Vector2i) -> Flower): Flower = { seed ->
  val state = scrollingState(seed.bag[key])
  input(Vector2i(0, -state.offset))(seed)
}

fun scrollingInteraction(key: String, contentBounds: Bounds): LogicModule = { bloomState, bounds ->
  val state = scrollingState(bloomState.bag[key])
  val input = bloomState.input
  val currentButton = input.current.mouseButtons[0]
  val previousButton = input.previous.mouseButtons[0]

  val (dragOrigin, offsetOrigin) = if (currentButton == ButtonState.down && previousButton == ButtonState.up
      && isInBounds(input.current.mousePosition, bounds))
    Pair(input.current.mousePosition, state.offset)
  else if (currentButton == ButtonState.up)
    Pair(null, state.offset)
  else
    Pair(state.dragOrigin, state.offsetOrigin)

  val offset = if (dragOrigin != null) {
    val mouseOffsetY = input.current.mousePosition.y - dragOrigin.y
    val mod = offsetOrigin + mouseOffsetY * contentBounds.dimensions.y / bounds.dimensions.y
//    println(mod)
    minMax(0, contentBounds.dimensions.y - bounds.dimensions.y, mod)
  } else
    state.offset

  val newState = ScrollingState(
      dragOrigin = dragOrigin,
      offsetOrigin = offsetOrigin,
      offset = offset
  )
  mapOf(key to newState)
}

fun scrollBox(key: String, contentBounds: Bounds): Flower = { b ->
  listOf(
      Box(
          bounds = b.bounds,
          depiction = scrollbar(scrollingState(b.bag[key]).offset, contentBounds.dimensions.y),
          logic = scrollingInteraction(key, contentBounds)
      )
  )
}

fun scrolling(key: String): (Flower) -> Flower = { child ->
  joinChildren(
      clipChildren(extractOffset(key, offset(child))),
      { scrollBox(key, it.bounds) }
  )
}