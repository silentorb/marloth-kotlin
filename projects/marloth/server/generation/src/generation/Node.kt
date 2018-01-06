package generation

import mythic.spatial.Vector3

class Connection(
    val first: Node,
    val second: Node
)

class Node(var position: Vector3, var radius: Float) {
  val connections: MutableList<Connection> = mutableListOf()
}