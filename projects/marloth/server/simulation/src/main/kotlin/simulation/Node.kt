package simulation

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3

data class Node(
    val id: Id,
    val position: Vector3,
    val height: Float,
    val isWalkable: Boolean,
    val biome: Biome,
    val isSolid: Boolean,
    val floors: List<FlexibleFace>,
    val ceilings: List<FlexibleFace>,
    val walls: List<FlexibleFace>
) {
  val horizontalNeighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }
  val neighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }

  val faces: List<FlexibleFace>
    get() = floors.plus(walls).plus(ceilings)
}