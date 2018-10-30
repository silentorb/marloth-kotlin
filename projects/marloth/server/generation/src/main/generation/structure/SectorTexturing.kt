package generation.structure

import generation.BiomeMap
import generation.abstract.Realm
import generation.abstract.faceNodes
import scenery.Textures
import simulation.*

fun determineFloorTexture(nodes: NodeTable, biomeMap: BiomeMap, info: NodeFace): Textures? {
  val first = nodes[info.firstNode]!!
  return if (first.isWalkable)
    biomeInfoMap[biomeMap[first.id]!!]!!.floorTexture
  else
    null
}

fun determineWallTexture(nodeTable: NodeTable, biomeMap: BiomeMap, info: NodeFace): Textures? {
  val nodes = faceNodes(info).map { nodeTable[it]!! }
      .filterNotNull()

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

fun determineCeilingTexture(nodes: NodeTable, biomeMap: BiomeMap, info: NodeFace): Textures? {
  val first = nodes[info.firstNode]!!
  val second = nodes[info.secondNode]
  return if (second != null && second.isSolid)
    biomeInfoMap[biomeMap[first.id]!!]!!.ceilingTexture
  else
    null
}

fun determineFaceTexture(nodes: NodeTable, biomeMap: BiomeMap, info: NodeFace): Textures? {
  return when (info.faceType) {
    FaceType.wall -> determineWallTexture(nodes, biomeMap, info)
    FaceType.floor -> determineFloorTexture(nodes, biomeMap, info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(nodes, biomeMap, info)
  }
}

fun assignTextures(nodes: NodeTable, faces: FaceTable, biomeMap: BiomeMap, realm: Realm) {
  realm.mesh.faces.forEach { face ->
    val info = faces[face.id]!!
    info.texture = determineFaceTexture(nodes, biomeMap, info)
  }
}
