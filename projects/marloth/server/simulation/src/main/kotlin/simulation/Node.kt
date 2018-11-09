package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val height: Float,
    val isWalkable: Boolean,
    val biome: Biome,
    val isSolid: Boolean,
    val floors: MutableList<ImmutableFace>,
    val ceilings: MutableList<ImmutableFace>,
    val walls: MutableList<ImmutableFace>
) : Entity {

  val faces: List<ImmutableFace>
    get() = floors.plus(walls).plus(ceilings)
}

fun horizontalNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it.id]!!) }

fun nodeNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it.id]!!) }

fun getPathNeighbors(nodes: NodeTable, faces: ConnectionTable, node: Node) =
    nodeNeighbors(faces, node)
        .map { nodes[it]!! }
        .filter { it.isWalkable }

