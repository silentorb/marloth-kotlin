package simulation

import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3

data class Node(
    val id: Id,
    val position: Vector3,
    val height: Float,
    val isWalkable: Boolean,
    val biome: Biome,
    val isSolid: Boolean,
    val floors: List<ImmutableFace>,
    val ceilings: List<ImmutableFace>,
    val walls: List<ImmutableFace>
) {
  val horizontalNeighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }
  val neighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }

  val faces: List<ImmutableFace>
    get() = floors.plus(walls).plus(ceilings)
}

fun getPathNeighbors(node: Node) =
    node.neighbors.filter { it.isWalkable }