package generation.architecture.building

import generation.architecture.*
import generation.architecture.definition.impassableHorizontal
import generation.misc.*
import generation.next.Builder
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import scenery.MeshName
import simulation.entities.ArchitectureElement
import kotlin.math.asin

fun floorMeshBuilder(mesh: MeshName, offset: Vector3 = Vector3.zero,
                     orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.config
  val biome = input.biome
  listOf(newArchitectureMesh(
      architecture = ArchitectureElement(isWall = false),
      meshes = config.meshes,
      mesh = mesh,
      position = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh) + offset,
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(down = impassableHorizontal, builder = floorMeshBuilder(mesh, offset, orientation))

fun halfFloorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun diagonalHalfFloorMesh(mesh: MeshName) =
    blockBuilder { input ->
      val angle = applyTurns(input.turns)
//      val position = Vector3(-5f, 5f, -0.01f).transform(Matrix().rotateZ(angle))
      val orientation = Quaternion()
//          .rotateZ(angle - Pi * 0.25f)
      floorMeshBuilder(mesh, offset = Vector3(), orientation = orientation)(input)
    }

fun newSlopedFloorMesh(mesh: MeshName) = blockBuilder(down = impassableHorizontal) { input ->
  val meshInfo = input.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 4f / meshInfo.shape.x)
  val orientation = Quaternion()
      .rotateZ(applyTurns(input.turns))
      .rotateX(slopeAngle)
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, cellLength / 8f), orientation = orientation)(input)
}
