package generation.structure

import generation.abstracted.faceNodes
import physics.voidNodeId
import scenery.Textures
import simulation.*

fun determineFloorTexture(nodes: NodeTable, info: ConnectionFace): Textures? {
  val first = nodes[info.firstNode]
  return if (first != null && first.isWalkable)
    biomeInfoMap[first.biome]!!.floorTexture
  else
    null
}

fun determineWallTexture(nodeTable: NodeTable, info: ConnectionFace): Textures? {
  val nodes = faceNodes(info)
      .filter { it != voidNodeId }
      .map { nodeTable[it]!! }

  assert(nodes.any())
  return if (nodes.size == 1) {
//    if (nodes.first().isSolid)
//      Textures.checkers
//    else
    null//Textures.debugCyan
  } else {
    val wallCount = nodes.count { it.isSolid }
    val walkableCount = nodes.count { it.isWalkable }
    if (wallCount > 0 && walkableCount != 2)
      biomeInfoMap[nodes.first().biome]!!.wallTexture
    else
      null
  }
}

fun determineCeilingTexture(nodes: NodeTable, info: ConnectionFace): Textures? {
  val first = nodes[info.firstNode]
  val second = nodes[info.secondNode]
  return if (first != null && second != null && second.isSolid)
    biomeInfoMap[first.biome]!!.ceilingTexture
  else
    null
}

fun determineFaceTexture(nodes: NodeTable, info: ConnectionFace): Textures? {
  if (info.id == 41L) {
    val k = 0
  }

  if (info.id == 98L) {
    val k = 0
  }
  return when (info.faceType) {
    FaceType.wall -> determineWallTexture(nodes, info)
    FaceType.floor -> determineFloorTexture(nodes, info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(nodes, info)
  }
}

fun assignTextures(nodes: NodeTable, faces: ConnectionTable) =
    faces.mapValues { (_, face) ->
      face.copy(texture = determineFaceTexture(nodes, face))
    }

