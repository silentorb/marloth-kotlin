package generation.structure

import scenery.Textures
import simulation.*

fun determineFloorTexture(info: FaceInfo): Textures? {
  val first = info.firstNode!!
  return if (first.isWalkable)
    first.biome.floorTexture
  else
    null
}

fun determineWallTexture(info: FaceInfo): Textures? {
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
      nodes.first().biome.wallTexture
    else
      null
  }
}

fun determineCeilingTexture(info: FaceInfo): Textures? {
  val first = info.firstNode!!
  val second = info.secondNode
  return if (second != null && second.isSolid)
    first.biome.ceilingTexture
  else
    null
}

fun determineFaceTexture(info: FaceInfo): Textures? {
  return when (info.type) {
    FaceType.wall -> determineWallTexture(info)
    FaceType.floor -> determineFloorTexture(info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(info)
  }
}

fun assignTextures(abstractWorld: AbstractWorld) {
  abstractWorld.mesh.faces.forEach { face ->
    val info = getFaceInfo(face)
    info.texture = determineFaceTexture(info)
  }
}
