package mythic.bloom

import mythic.spatial.Vector4
import mythic.typography.Font
import mythic.typography.TextStyle

data class Seed(
    val bag: StateBag = mapOf(),
    val bounds: Bounds,
    val clipBounds: Bounds? = null
)

typealias Flower = (Seed) -> Boxes

operator fun Flower.plus(b: Flower): Flower = { seed ->
  this(seed)
      .plus(b(seed))
}

fun depict(depiction: StateDepiction): Flower = { s ->
  listOf(
      Box(
          bounds = s.bounds,
          depiction = depiction(s)
      )
  )
}

fun depict(depiction: Depiction): Flower =
    depict { s: Seed -> depiction }

typealias StateDepiction = (Seed) -> Depiction

fun <T> getExistingOrNewState(initializer: () -> T): (Any?) -> T = { state ->
  if (state != null)
    state as T
  else
    initializer()
}

val emptyFlower: Flower = { listOf() }
