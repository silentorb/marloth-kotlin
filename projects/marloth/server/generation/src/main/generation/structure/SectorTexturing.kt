package generation.structure

import generation.BiomeMap
import generation.abstract.faceNodes
import physics.voidNodeId
import scenery.Textures
import simulation.*

fun determineFloorTexture(nodes: NodeTable, biomeMap: BiomeMap, info: ConnectionFace): Textures? {
  val first = nodes[info.firstNode]!!
  return if (first.isWalkable)
    biomeInfoMap[biomeMap[first.id]!!]!!.floorTexture
  else
    null
}

fun determineWallTexture(nodeTable: NodeTable, biomeMap: BiomeMap, info: ConnectionFace): Textures? {
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
      biomeInfoMap[biomeMap[nodes.first().id]!!]!!.wallTexture
    else
      null
  }
}

fun determineCeilingTexture(nodes: NodeTable, biomeMap: BiomeMap, info: ConnectionFace): Textures? {
  val first = nodes[info.firstNode]!!
  val second = nodes[info.secondNode]
  return if (second != null && second.isSolid)
    biomeInfoMap[biomeMap[first.id]!!]!!.ceilingTexture
  else
    null
}

fun determineFaceTexture(nodes: NodeTable, biomeMap: BiomeMap, info: ConnectionFace): Textures? {
  return when (info.faceType) {
    FaceType.wall -> determineWallTexture(nodes, biomeMap, info)
    FaceType.floor -> determineFloorTexture(nodes, biomeMap, info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(nodes, biomeMap, info)
  }
}

fun assignTextures(nodes: NodeTable, faces: ConnectionTable, biomeMap: BiomeMap) =
    faces.mapValues { (_, face) ->
      face.copy(texture = determineFaceTexture(nodes, biomeMap, face))
    }

