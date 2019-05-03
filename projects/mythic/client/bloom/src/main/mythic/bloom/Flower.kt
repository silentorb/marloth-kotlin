package mythic.bloom

fun maxBounds(a: Bounds, b: Bounds): Bounds {
  val x1 = Math.min(a.position.x, b.position.x)
  val y1 = Math.min(a.position.y, b.position.y)
  val x2 = Math.max(a.position.x + a.dimensions.x, b.position.x + b.dimensions.x)
  val y2 = Math.max(a.position.y + a.dimensions.y, b.position.y + b.dimensions.y)
  return Bounds(x1, y1, x2 - x1, y2 - y1)
}

data class Blossom(
    val boxes: FlatBoxes,
    val bounds: Bounds
) {
  operator fun plus(other: Blossom) = this.copy(
      boxes = boxes.plus(other.boxes),
      bounds = maxBounds(bounds, other.bounds)
  )

  fun append(other: Blossom) = this.copy(
      boxes = boxes.plus(other.boxes)
  )

  operator fun plus(other: FlatBoxes) = this.copy(
      boxes = boxes.plus(other)
  )
}

fun newBlossom(box: FlatBox): Blossom =
    Blossom(
        boxes = listOf(box),
        bounds = box.bounds
    )

val emptyBlossom =
    Blossom(
        boxes = listOf(),
        bounds = emptyBounds
    )

//fun FlowerOld.plus(b: FlowerOld): FlowerOld = { seed ->
//  this(seed) + b(seed)
//}


fun depict(depiction: StateDepiction): FlowerOld = { s ->
  newBlossom(FlatBox(
      bounds = s.bounds,
      depiction = depiction(s)
  ))
}

fun depict(depiction: Depiction): FlowerOld =
    depict { s: SeedOld -> depiction }

typealias StateDepiction = (SeedOld) -> Depiction

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

val emptyFlowerOld: FlowerOld = { emptyBlossom }
