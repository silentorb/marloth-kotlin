package generation.architecture.building

import generation.architecture.misc.Builder
import generation.architecture.old.*
import generation.general.Block
import generation.general.Side
import generation.general.TextureGroup
import generation.general.biomeTexture
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import kotlin.math.asin

fun floorMeshBuilder(mesh: MeshName, offset: Vector3 = Vector3.zero,
                     orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val biome = input.biome
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      mesh = mesh,
      position = Vector3(0f, 0f, -cellHalfLength) + align(config.meshes, alignWithCeiling)(mesh) + offset + Vector3(0f, 0f, 0.01f),
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun halfFloorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun diagonalHalfFloorMesh(mesh: MeshName, height: Float) =
    blockBuilder { input ->
//      val position = Vector3(-5f, 5f, 0f).transform(Matrix.identity.rotateZ(angle))
//      val position = Vector3(-5f, -5f, -0.01f)
      val position = Vector3(0f, 0f, height)
      floorMeshBuilder(mesh, offset = position)(input)
    }

fun newSlopedFloorMesh(mesh: MeshName, height: Float) = BlockBuilder(
    block = Block(
        sides = sides()
    )
) { input ->
  val meshInfo = input.general.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 4f / meshInfo.shape!!.x) - 0.007f
  val orientation = Quaternion()
//      .rotateZ(applyTurnsOld(input.turns))
      .rotateX(slopeAngle)
//  listOf()
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, height + cellLength / 8f), orientation = orientation)(input)
}

fun newSlopeEdgeBlock(mesh: MeshName, height: Float, openPattern: Side, ledgeTurns: Int) = BlockBuilder(
    block = Block(
        sides = mapOf(
            getTurnDirection(ledgeTurns) to openPattern
        ),
        slots = listOf(
//            Vector3(cellLength * (0.5f - ledgeTurns.toFloat() * 0.25f), cellLength * 0.25f, height)
            Vector3(cellLength * 0.25f, cellLength * (0.5f + ledgeTurns.toFloat() * 0.25f), height - quarterStep)
        )
    )
) { input ->
//  val side = getTurnedSide(input.sides, input.turns + turns)!!
//  if (side.any { openPattern.contains(it) }) {
//  val orientation = Quaternion().rotateZ(applyTurnsOld(input.turns + 1))
  val offset = Quaternion().rotateZ(applyTurnsOld(ledgeTurns))
      .transform(Vector3(0f, cellLength / 4f, 0f))
  val position = offset + Vector3(0f, 0f, height)
  floorMeshBuilder(mesh, offset = position)(input)
//  listOf()
//  } else
//    listOf()
}
