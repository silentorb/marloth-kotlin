package generation

import mythic.spatial.Vector3
import org.joml.Intersectionf
import org.joml.minus
import org.joml.plus
import randomly.Dice

fun overlaps2D(node: Node, other: Node) = Intersectionf.intersectCircleCircle(
    node.position.x, node.position.y, node.radius,
    other.position.x, other.position.y, other.radius,
    Vector3()
)

fun connectOverlapping(world: AbstractWorld) {
  for (node in world.nodes) {
    for (other in world.nodes) {
      if (node.isConnected(other))
        continue

      if (overlaps2D(node, other)) {
        world.connect(node, other, ConnectionType.union)
      }
    }
  }
}

class Generator(val world: AbstractWorld, val dice: Dice) {

  fun createNode(): Node {
    val radius = dice.getFloat(5f, 10f)
    val start = world.boundary.start + radius
    val end = world.boundary.end - radius
    val node = Node(
        Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
        radius
    )
    world.nodes.add(node)
    return node
  }

  fun createNodes(count: Int) {
    for (i in 0..count) {
      createNode()
    }
  }

  fun generate(): AbstractWorld {
    createNodes(20)
    connectOverlapping(world)

    return world
  }
}