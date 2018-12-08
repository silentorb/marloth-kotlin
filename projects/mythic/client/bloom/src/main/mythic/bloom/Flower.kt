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

data class IndexedTextStyle(
    val font: Int,
    val size: Float,
    val color: Vector4
)

typealias TextStyleSource = (List<Font>) -> TextStyle

fun resolve(style: IndexedTextStyle): TextStyleSource = { fonts: List<Font> ->
  TextStyle(
      font = fonts[style.font],
      size = style.size,
      color = style.color
  )
}

//fun padding(all: Int): (Flower) -> Flower = { flower ->
//  {seed->
//    flower()
//  }
//}
