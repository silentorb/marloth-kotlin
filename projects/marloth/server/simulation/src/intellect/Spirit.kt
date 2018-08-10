package intellect

import physics.Body
import simulation.Character
import simulation.Node

enum class SpiritMode {
  attacking,
  idle,
  moving
}

data class SpiritState(
    val mode: SpiritMode = SpiritMode.idle,
    val path: List<Node>? = null,
    val target: Character? = null
)

class Spirit(
    val character: Character,
    var state: SpiritState
) {
  val body: Body
    get() = character.body
}