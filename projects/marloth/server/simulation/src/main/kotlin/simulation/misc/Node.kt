package simulation.misc

import mythic.ent.WithId
import mythic.ent.Id
import mythic.spatial.Vector3

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val isWalkable: Boolean,
    val biome: Any? = null,
    val isSolid: Boolean,
    val floors: MutableList<Id> = mutableListOf(),
    val ceilings: MutableList<Id> = mutableListOf(),
    val walls: MutableList<Id> = mutableListOf()
) : WithId {

  val faces: List<Id>
    get() = floors.plus(walls).plus(ceilings)
}

fun horizontalNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node.id, faces[it]!!) }

fun nodeNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node.id, faces[it]!!) }

fun nodeNeighbors(nodes: NodeTable, faces: ConnectionTable, node: Id) = nodeNeighbors(faces, nodes[node]!!)

fun nodeNeighbors(faces: ConnectionTable, id: Id) = faces.mapNotNull { it.value.other(id) }

fun nodeNeighbors(realm: Realm, id: Id): Collection<Id> {
  return realm.nodeFaces[id]!!
      .mapNotNull { getOtherNode(id, realm.faces[it]!!) }
}

fun getPathNeighbors(nodes: NodeTable, faces: ConnectionTable, node: Id) =
    nodeNeighbors(nodes, faces, node)
        .map { nodes[it]!! }
        .filter { it.isWalkable }

typealias OneToManyMap = Map<Id, List<Id>>

fun mapNodeFaces(nodes: NodeTable, connections: ConnectionTable): OneToManyMap =
    nodes.mapValues { (_, node) -> connections.filter { it.value.nodes.contains(node.id) }.map { it.key } }
