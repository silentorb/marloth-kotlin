package generation.architecture.building

import generation.architecture.definition.impassableHorizontal
import generation.architecture.old.*
import generation.architecture.misc.Builder
import generation.general.*
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.scenery.MeshName
import simulation.misc.cellLength
import simulation.misc.floorOffset
import kotlin.math.asin

fun floorMeshBuilder(mesh: MeshName, offset: Vector3 = Vector3.zero,
                     orientation: Quaternion = Quaternion()): Builder = { input ->
  val config = input.general.config
  val biome = input.biome
  listOf(newArchitectureMesh(
      meshes = config.meshes,
      mesh = mesh,
      position = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh) + offset + Vector3(0f, 0f, 0.01f),
      orientation = orientation,
      texture = biomeTexture(biome, TextureGroup.floor)
  ))
}

fun floorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(down = impassableHorizontal, builder = floorMeshBuilder(mesh, offset, orientation))

fun halfFloorMesh(mesh: MeshName, offset: Vector3 = Vector3.zero, orientation: Quaternion = Quaternion()) =
    blockBuilder(builder = floorMeshBuilder(mesh, offset, orientation))

fun diagonalHalfFloorMesh(mesh: MeshName, height: Float) =
    blockBuilder { input ->
      val angle = applyTurns(input.turns)
//      val position = Vector3(-5f, 5f, 0f).transform(Matrix.identity.rotateZ(angle))
//      val position = Vector3(-5f, -5f, -0.01f)
      val position = Vector3(0f, 0f, height)
      val orientation = Quaternion()
          .rotateZ(angle)
      floorMeshBuilder(mesh, offset = position, orientation = orientation)(input)
    }

fun newSlopedFloorMesh(mesh: MeshName, height: Float) = blockBuilder(down = impassableHorizontal) { input ->
  val meshInfo = input.general.config.meshes[mesh]!!
  val slopeAngle = asin(cellLength / 4f / meshInfo.shape!!.x) - 0.007f
  val orientation = Quaternion()
      .rotateZ(applyTurns(input.turns))
      .rotateX(slopeAngle)
//  listOf()
  floorMeshBuilder(mesh, offset = Vector3(0f, 0f, height + cellLength / 8f), orientation = orientation)(input)
}

fun newSlopeEdgeBlock(mesh: MeshName, height: Float, openPattern: Side, turns: Int) = BlockBuilder(
    block = Block(
        sides = mapOf(
            getTurnDirection(turns) to openPattern
        ),
        slots = listOf(
//            Vector3(cellLength * 0.25f, cellLength * 0.25f, height)
            Vector3(cellLength * (0.5f - turns.toFloat() * 0.25f), cellLength * 0.25f, height)
        )
    )
) { input ->
//  val side = getTurnedSide(input.sides, input.turns + turns)!!
//  if (side.any { openPattern.contains(it) }) {
  val orientation = Quaternion().rotateZ(applyTurns(input.turns + 1))
  val offset = Quaternion().rotateZ(applyTurns(input.turns + turns))
      .transform(Vector3(0f, cellLength / 4f, 0f))
  val position = offset + Vector3(0f, 0f, height)
  floorMeshBuilder(mesh, offset = position, orientation = orientation)(input)
//  } else
//    listOf()
}
