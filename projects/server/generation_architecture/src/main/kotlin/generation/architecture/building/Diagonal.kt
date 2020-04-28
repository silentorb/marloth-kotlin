package generation.architecture.building

import generation.architecture.old.applyTurnsOld
import simulation.misc.cellHalfLength
import generation.architecture.definition.impassableHorizontal
import generation.architecture.definition.impassableVertical
import generation.general.Block
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute

fun randomDiagonalWall(height: Float = 0f) = blockBuilder { input ->
  val config = input.general.config
  val biome = input.biome
  val position = input.position + Vector3(cellHalfLength) + Vector3(0f, 0f, height)
  val angle = applyTurnsOld(input.turns) + Pi
  val scale = Vector3(1.42f, 1f, 1f)
  val mesh = getTruncatedWallMesh(input, height)
  listOf(newWallInternal(config, mesh, position, angle - Pi * 0.25f, biome, scale = scale))
}

fun diagonalCornerFloor(height: Float) = BlockBuilder(
    block = Block(
        attributes = setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable),
        sides = sides(
            down = impassableVertical,
            west = impassableHorizontal,
            south = impassableHorizontal
        )
    )
) + diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal.name, height) +
    withWallLamp(0.7f)(randomDiagonalWall(height))
