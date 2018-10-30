package generation.structure

import generation.abstract.Realm
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import physics.voidNodeId
import randomly.Dice
import simulation.FaceType
import simulation.*

enum class VerticalDirection {
  down,
  up
}

interface VerticalFacing {
  val dir: VerticalDirection
  val dirMod: Float
  fun ceilings(node: Node): MutableList<ImmutableFace>
  fun floors(node: Node): MutableList<ImmutableFace>
  fun upperNode(faces: FaceTable, node: Node): Id
  fun wallVertices(face: ImmutableFace): WallVertices
}

class VerticalFacingUp : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.up
  override val dirMod: Float get() = 1f
  override fun ceilings(node: Node): MutableList<ImmutableFace> = node.ceilings
  override fun floors(node: Node): MutableList<ImmutableFace> = node.floors
  override fun upperNode(faces: FaceTable, node: Node): Id = getUpperNode(faces, node)
  override fun wallVertices(face: ImmutableFace): WallVertices = getWallVertices(face)
}

class VerticalFacingDown : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.down
  override val dirMod: Float get() = -1f
  override fun ceilings(node: Node): MutableList<ImmutableFace> = node.floors
  override fun floors(node: Node): MutableList<ImmutableFace> = node.ceilings
  override fun upperNode(faces: FaceTable, node: Node): Id = getLowerNode(faces, node)
  override fun wallVertices(face: ImmutableFace): WallVertices {
    val result = getWallVertices(face)
    return WallVertices(upper = result.lower, lower = result.upper)
  }
}

fun createVerticalNodes(realm: Realm, mesh: RealmMesh, middleNodes: List<Node>, roomNodes: List<Node>, dice: Dice,
                        facing: VerticalFacing, shouldBeSolid: (original: Node) -> Boolean): Pair<Graph, RealmMesh> {
  val newNodes = middleNodes.map { node ->
    val depth = wallHeight
    val offset = Vector3(0f, 0f, depth * facing.dirMod)

    createSecondaryNode(node.position + offset, realm.nextId, isSolid = shouldBeSolid(node))
  }

  val newFaces = middleNodes.zip(newNodes) { node, newNode ->
    assert(facing.ceilings(node).any())
    facing.ceilings(node).map { ceiling ->
      facing.floors(newNode).add(ceiling)
      val info = mesh.faces[ceiling.id]!!
      info.secondNode = newNode.id
      info
    }
  }.flatten()

  var graph = realm.graph
  newNodes.map { lowerNode ->
    graph = addSpaceNode(graph, realm.faces, lowerNode)
  }
  val newMesh = mesh.copy(faces = mesh.faces.plus(entityMap(newFaces)))

  return Pair(graph, newMesh)
}

fun getLowerNode(faces: FaceTable, node: Node) =
    getOtherNode(node, faces[node.floors.first().id]!!)!!

fun getUpperNode(faces: FaceTable, node: Node) =
    getOtherNode(node, faces[node.ceilings.first().id]!!)!!

fun createAscendingSpaceWalls(realm: Realm, nodeTable: NodeTable, nextFaceId: IdSource, nodes: List<Node>, facing: VerticalFacing): Connections {
  val walls = nodes.flatMap { it.walls }
  val depth = 6f
  val offset = Vector3(0f, 0f, depth * facing.dirMod)
  return walls
      .filter { upperWall ->
        realm.faces[upperWall.id]!!.secondNode != voidNodeId
      }
      .map { upperWall ->
        val info = realm.faces[upperWall.id]!!
        val nodeId = if (nodeTable[info.firstNode]!!.isWalkable)
          info.firstNode
        else
          info.secondNode

        val node = nodeTable[nodeId]!!
        val upperNode = nodeTable[facing.upperNode(realm.faces, node)]!!
        val otherUpperNode = getOtherNode(node, realm.faces[upperWall.id]!!)!!
        val otherUpNode = nodeTable[facing.upperNode(realm.faces, nodeTable[otherUpperNode]!!)]!!
        val firstEdge = facing.wallVertices(upperWall).upper
        val unorderedVertices = firstEdge.plus(firstEdge.map { it + offset })
        val emptyNode = if (!upperNode.isSolid)
          upperNode
        else
          otherUpNode

        val orderedVertices = sortWallVertices(emptyNode.position, unorderedVertices)
        val newWall = realm.mesh.createStitchedFace(nextFaceId(), orderedVertices)
        throw Error("Not implemented")
//        newWall.data = NodeFace(FaceType.wall, upperNode, otherUpNode, null, "lower")
        upperNode.walls.add(newWall)
        otherUpNode.walls.add(newWall)
        Connection(node.id, upperNode.id, ConnectionType.ceilingFloor, FaceType.floor)
      }
}

fun expandVertically(realm: Realm, mesh: RealmMesh, nodeTable: NodeTable, nextFaceId: IdSource, roomNodes: List<Node>, dice: Dice): Pair<Graph, RealmMesh> {
  val middleNodes = realm.nodes.toList()
  val isRoom = { node: Node -> roomNodes.contains(node) }
  val shouldBeSolids = mapOf(
      VerticalDirection.down to { node: Node ->
        if (isRoom(node))
          true
        else if (!node.isSolid)
          false
        else dice.getInt(0, 3) != 0
      },
      VerticalDirection.up to { node: Node ->
        dice.getInt(0, 4) != 0
//        val biome = biomeMap[node.id]!!
//        when (biome.enclosure) {
//          Enclosure.all -> dice.getInt(0, 4) != 0
//          Enclosure.none -> dice.getInt(0, 4) != 0
//          Enclosure.some -> dice.getInt(0, 4) != 0
//        }
//        if (!node.biome.hasEnclosedRooms || (!roomNodes.contains(node) && !node.isSolid))
//          false
//        else dice.getInt(0, 4) != 0
      })
  var graph = realm.graph
  var currentMesh = mesh
  listOf(VerticalFacingDown(), VerticalFacingUp())
      .forEach { facing ->
        val result = createVerticalNodes(realm.copy(graph = graph), currentMesh, middleNodes, roomNodes, dice, facing, shouldBeSolids[facing.dir]!!)
        graph = result.first
        currentMesh = result.second
        createAscendingSpaceWalls(realm, nodeTable, nextFaceId, middleNodes, facing)
      }

  return Pair(graph, currentMesh)
}
