package generation.structure

import generation.BiomeMap
import generation.abstract.FaceInfo
import generation.abstract.Realm
import generation.abstract.faceNodes
import generation.abstract.getFaceInfo
import scenery.Textures
import simulation.FaceType
import simulation.biomeInfoMap

fun determineFloorTexture(biomeMap: BiomeMap, info: FaceInfo): Textures? {
  val first = info.firstNode!!
  return if (first.isWalkable)
    biomeInfoMap[biomeMap[first.id]!!]!!.floorTexture
  else
    null
}

fun determineWallTexture(biomeMap: BiomeMap, info: FaceInfo): Textures? {
  val nodes = faceNodes(info)
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

fun determineCeilingTexture(biomeMap: BiomeMap, info: FaceInfo): Textures? {
  val first = info.firstNode!!
  val second = info.secondNode
  return if (second != null && second.isSolid)
    biomeInfoMap[biomeMap[first.id]!!]!!.ceilingTexture
  else
    null
}

fun determineFaceTexture(biomeMap: BiomeMap, info: FaceInfo): Textures? {
  return when (info.type) {
    FaceType.wall -> determineWallTexture(biomeMap, info)
    FaceType.floor -> determineFloorTexture(biomeMap, info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(biomeMap, info)
  }
}

fun assignTextures(biomeMap: BiomeMap, realm: Realm) {
  realm.mesh.faces.forEach { face ->
    val info = getFaceInfo(face)
    info.texture = determineFaceTexture(biomeMap, info)
  }
}
