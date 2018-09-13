package generation.structure

import generation.BiomeMap
import generation.abstract.*
import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3m
import org.joml.plus
import randomly.Dice
import simulation.FaceType

enum class VerticalDirection {
  down,
  up
}

interface VerticalFacing {
  val dir: VerticalDirection
  val dirMod: Float
  fun ceilings(node: Node): MutableList<FlexibleFace>
  fun floors(node: Node): MutableList<FlexibleFace>
  fun upperNode(node: Node): Node
  fun wallVertices(face: FlexibleFace): WallVertices
}

class VerticalFacingUp : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.up
  override val dirMod: Float get() = 1f
  override fun ceilings(node: Node): MutableList<FlexibleFace> = node.ceilings
  override fun floors(node: Node): MutableList<FlexibleFace> = node.floors
  override fun upperNode(node: Node): Node = getUpperNode(node)
  override fun wallVertices(face: FlexibleFace): WallVertices = getWallVertices(face)
}

class VerticalFacingDown : VerticalFacing {
  override val dir: VerticalDirection = VerticalDirection.down
  override val dirMod: Float get() = -1f
  override fun ceilings(node: Node): MutableList<FlexibleFace> = node.floors
  override fun floors(node: Node): MutableList<FlexibleFace> = node.ceilings
  override fun upperNode(node: Node): Node = getLowerNode(node)
  override fun wallVertices(face: FlexibleFace): WallVertices {
    val result = getWallVertices(face)
    return WallVertices(upper = result.lower, lower = result.upper)
  }
}

fun createVerticalNodes(realm: Realm, middleNodes: List<Node>, roomNodes: List<Node>, dice: Dice,
                        facing: VerticalFacing, shouldBeSolid: (original: Node) -> Boolean): Graph {
  val newNodes = middleNodes.map { node ->
    val depth = wallHeight
    val offset = Vector3m(0f, 0f, depth * facing.dirMod)

    val newNode = createSecondaryNode(node.position + offset, realm.nextId, isSolid = shouldBeSolid(node))
    assert(facing.ceilings(node).any())
    for (ceiling in facing.ceilings(node)) {
      facing.floors(newNode).add(ceiling)
      val info = getFaceInfo(ceiling)
      info.secondNode = newNode
    }
    newNode
  }

  var graph = realm.graph
  newNodes.map { lowerNode ->
    graph = addSpaceNode(graph, lowerNode)
  }

  return graph
}

fun getLowerNode(node: Node) =
    getOtherNode(node, node.floors.first())!!

fun getUpperNode(node: Node) =
    getOtherNode(node, node.ceilings.first())!!

fun createAscendingSpaceWalls(realm: Realm, nodes: List<Node>, facing: VerticalFacing): Connections {
  val walls = nodes.flatMap { it.walls }
  val depth = 6f
  val offset = Vector3m(0f, 0f, depth * facing.dirMod)
  return walls
      .filter { upperWall ->
        getFaceInfo(upperWall).secondNode != null
      }
      .map { upperWall ->
        val info = getFaceInfo(upperWall)
        val node = if (info.firstNode!!.isWalkable)
          info.firstNode
        else
          info.secondNode!!

        val upperNode = facing.upperNode(node)
        val otherUpperNode = getOtherNode(node, upperWall)!!
        val otherUpNode = facing.upperNode(otherUpperNode)
        val firstEdge = facing.wallVertices(upperWall).upper
        val unorderedVertices = firstEdge.plus(firstEdge.map { it + offset })
        val emptyNode = if (!upperNode.isSolid)
          upperNode
        else
          otherUpNode

        val orderedVertices = sortWallVertices(emptyNode.position, unorderedVertices)
        val newWall = realm.mesh.createStitchedFace(orderedVertices)
        newWall.data = FaceInfo(FaceType.wall, upperNode, otherUpNode, null, "lower")
        upperNode.walls.add(newWall)
        otherUpNode.walls.add(newWall)
        Connection(node.id, upperNode.id, ConnectionType.ceilingFloor)
      }
}

fun expandVertically(realm: Realm, roomNodes: List<Node>, dice: Dice): Graph {
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
  listOf(VerticalFacingDown(), VerticalFacingUp())
      .forEach { facing ->
        graph = createVerticalNodes(realm.copy(graph = graph), middleNodes, roomNodes, dice, facing, shouldBeSolids[facing.dir]!!)
        createAscendingSpaceWalls(realm, middleNodes, facing)
      }

  return graph
}
