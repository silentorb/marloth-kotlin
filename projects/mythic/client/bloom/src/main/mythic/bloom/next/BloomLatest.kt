package mythic.bloom.next

import mythic.bloom.*
import org.joml.Vector2i
import org.joml.minus
import org.joml.plus

private val emptyBoxList: List<Box> = listOf()

data class Box(
    val name: String = "",
    val bounds: Bounds,
    val boxes: List<Box> = emptyBoxList,
    val depiction: Depiction? = null,
    val clipBounds: Bounds? = null,
    val handler: Any? = null,
    val logic: LogicModule? = null
)

data class Seed(
    val bag: StateBag = mapOf(),
    val dimensions: Vector2i,
    val clipBounds: Bounds? = null
)

typealias Flower = (Seed) -> Box

typealias FlowerWrapper = (Flower) -> Flower

typealias ForwardLayout = (Vector2i) -> Bounds

typealias ReverseLayout = (Vector2i, Bounds, Bounds) -> Bounds

private val forwardPass: ForwardLayout = { Bounds(dimensions = it) }
private val reversePass: ReverseLayout = { _, bounds, _ -> bounds }

val emptyBox = Box(
    bounds = Bounds(
        dimensions = Vector2i()
    )
)

val emptyFlower: Flower = { emptyBox }

fun div(name: String = "",
        forward: ForwardLayout = forwardPass,
        reverse: ReverseLayout = reversePass,
        depiction: Depiction? = null,
        logic: LogicModule? = null): FlowerWrapper = { flower ->
  { seed ->
    val bounds = forward(seed.dimensions)
    val childSeed = seed.copy(
        dimensions = bounds.dimensions
    )
    val childBox = flower(childSeed)
    val finalBounds = reverse(seed.dimensions, bounds, childBox.bounds)

    Box(
        name = name,
        bounds = finalBounds,
        boxes = listOf(childBox),
        depiction = depiction,
        logic = logic
    )
  }
}

fun compose(flowers: List<Flower>): Flower = { seed ->
  Box(
      bounds = Bounds(dimensions = seed.dimensions),
      boxes = flowers.map { it(seed) }
  )
}

fun compose(vararg flowers: Flower): Flower = { seed ->
  Box(
      bounds = Bounds(dimensions = seed.dimensions),
      boxes = flowers.map { it(seed) }
  )
}

infix fun Flower.plusFlower(second: Flower): Flower =
    compose(this, second)

//fun flattenBoxes(includeEmpty: Boolean, box: Box, parentOffset: Vector2i): List<FlatBox> {
//  val offset = parentOffset + box.bounds.position
//  val children = box.boxes.flatMap { child ->
//    flattenBoxes(includeEmpty, child, offset)
//  }
//  return if (includeEmpty || box.depiction != null || box.handler != null || box.logic != null)
//    listOf(FlatBox(
//        bounds = box.bounds.copy(
//            position = offset
//        ),
//        depiction = box.depiction,
//        clipBounds = box.clipBounds,
//        handler = box.handler,
//        logic = box.logic
//    ))
//        .plus(children)
//  else
//    children
//}

fun dependentBoundsTransform(transform: (Vector2i, Bounds, Bounds) -> Vector2i): ReverseLayout = { parent, bounds, child ->
  val offset = transform(parent, bounds, child)
  moveBounds(offset, bounds.dimensions)(child)
}

fun fixedOffset(offset: Vector2i): ForwardLayout = { container ->
  val newDimensions = clippedDimensions(container, offset, container)
  Bounds(
      position = offset,
      dimensions = newDimensions
  )
}

fun fixedOffset(left: Int = 0, top: Int = 0): ForwardLayout = fixedOffset(Vector2i(left, top))

fun fixed(value: Int): PlanePositioner = { plane -> { value } }

fun forwardOffset(left: PlanePositioner? = null,
                  top: PlanePositioner? = null): ForwardLayout = { container ->
  val position = Vector2i(
      if (left != null) left(horizontalPlaneMap)(container) else 0,
      if (top != null) top(verticalPlaneMap)(container) else 0
  )

  Bounds(
      position = position,
      dimensions = container
  )
}

fun forwardDimensions(
    width: PlanePositioner? = null,
    height: PlanePositioner? = null): ForwardLayout = { container ->
  val dimensions = Vector2i(
      x = if (width != null) width(horizontalPlaneMap)(container) else container.x,
      y = if (height != null) height(verticalPlaneMap)(container) else container.y
  )

  Bounds(
      dimensions = dimensions
  )
}

infix fun Flower.plusLogic(logic: LogicModule): Flower = { seed ->
  val box = this(seed)
  val newLogic = if (box.logic == null)
    logic
  else
    box.logic combineLogic logic

  box.copy(
      logic = newLogic
  )
}

fun ForwardLayout.plus2(other: ForwardLayout): ForwardLayout = { container ->
  val a = this(container)
  val b = other(a.dimensions)
  Bounds(
      position = a.position + b.position,
      dimensions = b.dimensions
  )
}

operator fun ReverseLayout.plus(other: ReverseLayout): ReverseLayout = { parent, bounds, child ->
  val a = this(parent, bounds, child)
  other(parent, a, child)
}

fun reverseOffset(left: ReversePlanePositioner? = null,
                  top: ReversePlanePositioner? = null): ReverseLayout = { parent, bounds, child ->
  bounds.copy(
      position = Vector2i(
          if (left != null) left(horizontalPlaneMap)(parent, bounds, child) else bounds.position.x,
          if (top != null) top(verticalPlaneMap)(parent, bounds, child) else bounds.position.y
      )
  )
}

fun reverseDimensions(width: ReversePlanePositioner? = null,
                      height: ReversePlanePositioner? = null): ReverseLayout = { parent, bounds, child ->
  bounds.copy(
      dimensions = Vector2i(
          if (width != null) width(horizontalPlaneMap)(parent, bounds, child) else bounds.dimensions.x,
          if (height != null) height(verticalPlaneMap)(parent, bounds, child) else bounds.dimensions.y
      )
  )
}

val shrink: ReverseLayout = { parent, bounds, child ->
  bounds.copy(
      dimensions = child.dimensions
  )
}

val shrinkWrap: ReversePlanePositioner = { plane ->
  { parent, bounds, child ->
    plane.x(child.dimensions)
  }
}

fun margin(all: Int = 0, left: Int = all, top: Int = all, bottom: Int = all, right: Int = all): FlowerWrapper = div(
    reverse = { _, bounds, child ->
      bounds.copy(
          position = Vector2i(left, top),
          dimensions = child.dimensions + Vector2i(left + right, top + bottom)
      )
    }
)

fun padding2(all: Int = 0, left: Int = all, top: Int = all, bottom: Int = all, right: Int = all): FlowerWrapper = div(
    forward = fixedOffset(left, top),
    reverse = { _, bounds, child ->
      bounds.copy(
          dimensions = child.dimensions + Vector2i(left + right, top + bottom)
      )
    }
)

fun padding(all: Int = 0, left: Int = all, top: Int = all, bottom: Int = all, right: Int = all): FlowerWrapper = { flower ->
  { seed ->
    val paddingWidth = left + right
    val paddingHeight = top + bottom
    val paddingDimensions = Vector2i(paddingWidth, paddingHeight)
    val childDimensions = seed.dimensions - paddingDimensions
    val childSeed = seed.copy(
        dimensions = childDimensions
    )
    val box = flower(childSeed)
    val offsetBox = box.copy(
        bounds = box.bounds.copy(
            position = box.bounds.position + Vector2i(left, top)
        )
    )
    Box(
        name = "padding",
        bounds = Bounds(dimensions = box.bounds.dimensions + paddingDimensions),
        boxes = listOf(offsetBox)
    )
  }
}

fun accumulatedBounds(boxes: List<Box>): Bounds {
  assert(boxes.any())
  val start = boxes.first().bounds.position
  val end = boxes.sortedByDescending { it.bounds.end.y }.first().bounds.end
  return Bounds(start, end - start)
}

fun depictBehind2(depiction: Depiction): (Flower) -> Flower = { flower ->
  { seed ->
    val box = flower(seed)
    val boxDepiction = box.depiction
    box.copy(
        depiction = { b, c ->
          depiction(b, c)
          if (boxDepiction != null)
            boxDepiction(b, c)
        }
    )
  }
}

infix fun Flower.depictBehind(depiction: Depiction): Flower =
    depictBehind2(depiction)(this)

fun breakReverse(child: Flower): Flower {
  return { seed ->
    val box = child(seed)
    Box(
        name = "breakReverse",
        bounds = Bounds(dimensions = Vector2i()),
        boxes = listOf(box)
    )
  }
}