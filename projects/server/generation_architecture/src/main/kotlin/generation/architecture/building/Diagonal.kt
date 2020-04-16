package generation.architecture.building

import generation.architecture.old.applyTurns
import simulation.misc.cellHalfLength
import generation.architecture.definition.impassableHorizontal
import generation.architecture.definition.impassableVertical
import generation.general.Side
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute

fun randomDiagonalWall(height: Float = 0f) = blockBuilder { input ->
  val config = input.general.config
  val biome = input.biome
  val position = input.position + Vector3(cellHalfLength) + Vector3(0f, 0f, height)
  val angle = applyTurns(input.turns) + Pi
  val scale = Vector3(1.42f, 1f, 1f)
  listOf(newWallInternal(config, MeshId.squareWall.name, position, angle - Pi * 0.25f, biome, scale = scale))
}

fun diagonalCornerFloor(openSide: Side, height: Float) =
    compose(setOf(CellAttribute.categoryDiagonal, CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            down = impassableVertical,
            east = openSide,
            north = openSide,
            west = impassableHorizontal,
            south = impassableHorizontal
        ),
        diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal.name, height),
        withWallLamp(0.7f)(randomDiagonalWall(height))
    )
