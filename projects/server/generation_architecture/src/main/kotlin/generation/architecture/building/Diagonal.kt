package generation.architecture.building

import generation.architecture.engine.Builder
import generation.architecture.matrical.mergeBuilders
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.cellHalfLength

fun diagonalWall(depiction: Depiction): Builder = { input ->
  val config = input.general.config
  val scale = Vector3(1.0f, 1f, 1f)
  listOf(newWallInternal(config, depiction, Vector3(0f, 0f, -cellHalfLength), Quaternion().rotateZ(Pi * 0.25f), scale = scale))
}

fun diagonalCornerBuilder(texture: TextureName): Builder = mergeBuilders(
    diagonalHalfFloorMesh(Depiction(mesh = MeshId.floorDiagonal, texture = texture)),
    diagonalWall(Depiction(mesh = MeshId.diagonalWall, texture = texture))
//    cubeWallsWithFeatures(fullWallFeatures(), lampOffset = plainWallLampOffset())
)
