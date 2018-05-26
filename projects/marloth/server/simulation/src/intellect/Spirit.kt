package intellect

import simulation.Body
import simulation.Node

enum class SpiritMode {
  idle,
  moving
}

data class SpiritState(
    val mode: SpiritMode = SpiritMode.idle,
    val path: List<Node>? = null
)

class Spirit(
    val character: simulation.Character,
    var state: SpiritState
) {
  val body: Body
    get() = character.body
}