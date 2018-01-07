package generation

import mythic.spatial.Vector3
import randomly.Dice

class Generator(val world: AbstractWorld, val dice: Dice) {

  fun createNode(): Node {
    val node = Node(
        Vector3(dice.getFloat(300f), dice.getFloat(300f), 0f),
        10f
    )
    world.nodes.add(node)
    return node
  }

  fun generate(): AbstractWorld {
    val first = createNode()
    val second = createNode()
    world.connect(first, second)

    for (i in 0..3) {
      createNode()
    }

    return world
  }
}