package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.spatial.Vector3

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val height: Float,
    val isWalkable: Boolean,
    val biome: Biome,
    val isSolid: Boolean,
    val floors: MutableList<Id> = mutableListOf(),
    val ceilings: MutableList<Id> = mutableListOf(),
    val walls: MutableList<Id> = mutableListOf()
) : Entity {

  val faces: List<Id>
    get() = floors.plus(walls).plus(ceilings)
}

fun horizontalNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it]!!) }

fun nodeNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it]!!) }

fun nodeNeighbors(realm: Realm, id: Id): Collection<Id> {
  val node = realm.nodeTable[id]!!
  return node.walls.mapNotNull { getOtherNode(node, realm.faces[it]!!) }
}

fun getPathNeighbors(nodes: NodeTable, faces: ConnectionTable, node: Node) =
    nodeNeighbors(faces, node)
        .map { nodes[it]!! }
        .filter { it.isWalkable }

