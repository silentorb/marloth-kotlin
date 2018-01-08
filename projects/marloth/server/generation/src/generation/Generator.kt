package generation

import mythic.spatial.Vector3
import randomly.Dice

class Generator(val world: AbstractWorld, val dice: Dice) {

  fun createNode(): Node {
    val start = world.boundary.start
    val end = world.boundary.end
    val node = Node(
        Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
        5f
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