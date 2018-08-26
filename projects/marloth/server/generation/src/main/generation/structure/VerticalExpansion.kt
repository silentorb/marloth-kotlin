package generation.structure

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3
import org.joml.plus
import randomly.Dice
import simulation.*


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

fun createVerticalNodes(abstractWorld: AbstractWorld, middleNodes: List<Node>, roomNodes: List<Node>, dice: Dice,
                        facing: VerticalFacing, shouldBeSolid: (original: Node) -> Boolean) {
  val newNodes = middleNodes.map { node ->
    val depth = 2f
    val offset = Vector3(0f, 0f, depth * facing.dirMod)

    val newNode = createSecondaryNode(node.position + offset, abstractWorld, isSolid = shouldBeSolid(node), biome = node.biome)
    assert(facing.ceilings(node).any())
    for (ceiling in facing.ceilings(node)) {
      facing.floors(newNode).add(ceiling)
      val info = getFaceInfo(ceiling)
      info.secondNode = newNode
    }
    newNode
  }

  newNodes.forEach { lowerNode ->
    addSpaceNode(abstractWorld, lowerNode)
  }
}

fun getLowerNode(node: Node) =
    getOtherNode(node, node.floors.first())!!

fun getUpperNode(node: Node) =
    getOtherNode(node, node.ceilings.first())!!

fun createAscendingSpaceWalls(abstractWorld: AbstractWorld, nodes: List<Node>, facing: VerticalFacing) {
  val walls = nodes.flatMap { it.walls }
  val depth = 6f
  val offset = Vector3(0f, 0f, depth * facing.dirMod)
  walls.forEach { upperWall ->
    val info = getFaceInfo(upperWall)
    if (info.secondNode != null) {
      val node = if (info.firstNode!!.isWalkable)
        info.firstNode!!
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
      val newWall = abstractWorld.mesh.createStitchedFace(orderedVertices)
      newWall.data = FaceInfo(FaceType.wall, upperNode, otherUpNode, null, "lower")
      upperNode.walls.add(newWall)
      otherUpNode.walls.add(newWall)
      abstractWorld.graph.connect(node, upperNode, ConnectionType.ceilingFloor)
    }
  }
}

fun expandVertically(abstractWorld: AbstractWorld, roomNodes: List<Node>, dice: Dice) {
  val middleNodes = abstractWorld.nodes.toList()
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
        if (!node.biome.hasEnclosedRooms || (!roomNodes.contains(node) && !node.isSolid))
          false
        else dice.getInt(0, 3) != 0
      }
  )
  listOf(VerticalFacingDown(), VerticalFacingUp())
      .forEach { facing ->
        createVerticalNodes(abstractWorld, middleNodes, roomNodes, dice, facing, shouldBeSolids[facing.dir]!!)
        createAscendingSpaceWalls(abstractWorld, middleNodes, facing)
      }
}
