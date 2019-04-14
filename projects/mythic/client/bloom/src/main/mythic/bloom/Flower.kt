package mythic.bloom

data class Seed(
    val bag: StateBag = mapOf(),
    val bounds: Bounds,
    val clipBounds: Bounds? = null
)

fun maxBounds(a: Bounds, b: Bounds): Bounds {
  val x1 = Math.min(a.position.x, b.position.x)
  val y1 = Math.min(a.position.y, b.position.y)
  val x2 = Math.max(a.position.x + a.dimensions.x, b.position.x + b.dimensions.x)
  val y2 = Math.max(a.position.y + a.dimensions.y, b.position.y + b.dimensions.y)
  return Bounds(x1, y1, x2 - x1, y2 - y1)
}

data class Blossom(
    val boxes: Boxes,
    val bounds: Bounds
) {
  fun plus(other: Blossom) = this.copy(
      boxes = boxes.plus(other.boxes),
      bounds = maxBounds(bounds, other.bounds)
  )

  fun plus(other: Boxes) = this.copy(
      boxes = boxes.plus(other)
  )
}

fun newBlossom(box: Box): Blossom =
    Blossom(
        boxes = listOf(box),
        bounds = box.bounds
    )

val emptyBlossom =
    Blossom(
        boxes = listOf(),
        bounds = emptyBounds
    )

typealias Flower = (Seed) -> Blossom

operator fun Flower.plus(b: Flower): Flower = { seed ->
  this(seed)
      .plus(b(seed))
}

fun addFlowers(flowers: List<Flower>) =
    flowers.reduce { a, b -> a.plus(b) }

fun depict(depiction: StateDepiction): Flower = { s ->
  newBlossom(Box(
      bounds = s.bounds,
      depiction = depiction(s)
  ))
}

fun depict(depiction: Depiction): Flower =
    depict { s: Seed -> depiction }

typealias StateDepiction = (Seed) -> Depiction

inline fun <reified T> existingOrNewState(crossinline initializer: () -> T): (Any?) -> T = { state ->
  if (state is T)
    state
  else
    initializer()
}

typealias BagGetter<T> = (StateBag) -> T

inline fun <reified T> existingOrNewState(key: String, crossinline initializer: () -> T): BagGetter<T> = { bag ->
  val value = bag[key]
  if (value is T)
    value
  else
    initializer()
}

val emptyFlower: Flower = { emptyBlossom }
