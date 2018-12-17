package generation.structure

import mythic.ent.Id
import mythic.ent.entityMap
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
  fun ceilings(node: Node): MutableList<Id>
  fun floors(node: Node): MutableList<Id>
  fun upperNode(faces: ConnectionTable, node: Node): Id
  fun wallVertices(face: ImmutableFace): WallVertices
}

class VerticalFacingUp : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.up
  override val dirMod: Float get() = 1f
  override fun ceilings(node: Node): MutableList<Id> = node.ceilings
  override fun floors(node: Node): MutableList<Id> = node.floors
  override fun upperNode(faces: ConnectionTable, node: Node): Id = getUpperNode(faces, node)
  override fun wallVertices(face: ImmutableFace): WallVertices = getWallVertices(face)
}

class VerticalFacingDown : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.down
  override val dirMod: Float get() = -1f
  override fun ceilings(node: Node): MutableList<Id> = node.floors
  override fun floors(node: Node): MutableList<Id> = node.ceilings
  override fun upperNode(faces: ConnectionTable, node: Node): Id = getLowerNode(faces, node)
  override fun wallVertices(face: ImmutableFace): WallVertices {
    val result = getWallVertices(face)
    return WallVertices(upper = result.lower, lower = result.upper)
  }
}

fun createVerticalNodes(idSources: StructureIdSources, realm: StructureRealm, middleNodes: Collection<Node>, roomNodes: Collection<Node>, dice: Dice,
                        facing: VerticalFacing, shouldBeSolid: (original: Node) -> Boolean): StructureRealm {
  val newNodes = middleNodes.map { node ->
    val depth = wallHeight
    val offset = Vector3(0f, 0f, depth * facing.dirMod)

    createSecondaryNode(node.position + offset, idSources.node,
        isSolid = shouldBeSolid(node),
        biome = node.biome
    )
  }

  val newFaces = middleNodes.zip(newNodes) { node, newNode ->
    assert(facing.ceilings(node).any())
    facing.ceilings(node).map { ceiling ->
      facing.floors(newNode).add(ceiling)
      val info = realm.connections[ceiling]!!
      info.copy(secondNode = newNode.id)
    }
  }.flatten()

  return StructureRealm(
      nodes = realm.nodes.plus(entityMap(newNodes)),
      connections = realm.connections.plus(entityMap(newFaces)),
      mesh = realm.mesh
  )
}

fun getLowerNode(faces: ConnectionTable, node: Node) =
    getOtherNode(node.id, faces[node.floors.first()]!!)!!

fun getUpperNode(faces: ConnectionTable, node: Node) =
    getOtherNode(node.id, faces[node.ceilings.first()]!!)!!

fun createAscendingSpaceWalls(idSources: StructureIdSources, realm: StructureRealm, nodes: Collection<Node>, facing: VerticalFacing): StructureRealm {
  val walls = nodes.flatMap { it.walls }
  val depth = 6f
  val offset = Vector3(0f, 0f, depth * facing.dirMod)
  val updatePairs = walls
      .filter { upperWall ->
        realm.connections[upperWall]!!.secondNode != voidNodeId
      }
      .map { upperWall ->
        val info = realm.connections[upperWall]!!
        val nodeId = if (realm.nodes[info.firstNode]!!.isWalkable)
          info.firstNode
        else
          info.secondNode

        val node = realm.nodes[nodeId]!!
        val upperNode = realm.nodes[facing.upperNode(realm.connections, node)]!!
        val otherUpperNode = getOtherNode(nodeId, realm.connections[upperWall]!!)!!
        val otherUpNode = realm.nodes[facing.upperNode(realm.connections, realm.nodes[otherUpperNode]!!)]!!
        val firstEdge = facing.wallVertices(realm.mesh.faces[upperWall]!!).upper
        val unorderedVertices = firstEdge.plus(firstEdge.map { it + offset })
        val emptyNode = if (!upperNode.isSolid)
          upperNode
        else
          otherUpNode

        val orderedVertices = sortWallVertices(emptyNode.position, unorderedVertices)
        val newWall = realm.mesh.createStitchedFace(idSources.edge, idSources.face(), orderedVertices)
//        newWall.data = ConnectionFace(FaceType.wall, upperNode, otherUpNode, null, "lower")
        upperNode.walls.add(newWall.id)
        otherUpNode.walls.add(newWall.id)
//        InitialConnection(node.id, upperNode.id, ConnectionType.ceilingFloor, FaceType.floor)
        val connection = ConnectionFace(newWall.id, FaceType.wall, upperNode.id, otherUpNode.id, null, "lower")
        FacePair(connection, newWall)
      }

  val (updatedConnections, updatedFaces) = splitFacePairTables(updatePairs)
  return StructureRealm(
      nodes = realm.nodes,
      connections = realm.connections.plus(updatedConnections),
      mesh = realm.mesh.copy(
          faces = realm.mesh.faces.plus(updatedFaces)
      )
  )
}

fun expandVertically(idSources: StructureIdSources, realm: StructureRealm, roomNodes: Collection<Node>, dice: Dice): StructureRealm {
  val middleNodes = realm.nodes.values
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
  var currentRealm = realm
  listOf(VerticalFacingDown(), VerticalFacingUp())
      .forEach { facing ->
        currentRealm = createVerticalNodes(idSources, currentRealm, middleNodes, roomNodes, dice, facing, shouldBeSolids[facing.dir]!!)
        currentRealm = createAscendingSpaceWalls(idSources, currentRealm, middleNodes, facing)
      }

  return currentRealm
}
