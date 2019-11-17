package generation.architecture.building

import generation.architecture.old.applyTurns
import generation.architecture.old.cellHalfLength
import generation.architecture.definition.impassableHorizontal
import generation.architecture.definition.impassableVertical
import generation.general.Side
import mythic.spatial.Pi
import mythic.spatial.Vector3
import scenery.enums.MeshId
import simulation.misc.NodeAttribute

fun randomDiagonalWall(height: Float = 0f) = blockBuilder { input ->
  val cell = input.cell
  val grid = input.grid
  val connections = input.grid.connections
  val config = input.config
  val biome = input.biome
  val dice = input.dice
  val position = input.position + Vector3(cellHalfLength) + Vector3(0f, 0f, height)
  val angle = applyTurns(input.turns)
  val scale = Vector3(1.42f, 1f, 1f)
  listOf(newWallInternal(config, MeshId.squareWall.name, position, angle - Pi * 0.25f, biome, scale = scale))
}

fun diagonalCornerFloor(openSide: Side, height: Float) =
    compose(
        blockBuilder(
            up = impassableVertical,
            down = impassableVertical,
            east = openSide,
            north = openSide,
            west = impassableHorizontal,
            south = impassableHorizontal,
            attributes = setOf(NodeAttribute.categoryDiagonal)
        ),
        diagonalHalfFloorMesh(MeshId.squareFloorHalfDiagonal.name, height),
        randomDiagonalWall(height)
    )
