package generation.architecture.building

import generation.architecture.applyTurns
import generation.architecture.cellHalfLength
import generation.architecture.definition.impassableHorizontal
import generation.architecture.definition.impassableVertical
import generation.architecture.definition.requiredOpen
import generation.elements.Side
import mythic.spatial.Pi
import mythic.spatial.Vector3
import scenery.enums.MeshId
import simulation.misc.NodeAttribute

fun randomDiagonalWall() = blockBuilder { input ->
  val cell = input.cell
  val grid = input.grid
  val connections = input.grid.connections
  val config = input.config
  val biome = input.biome
  val dice = input.dice
  val position = input.position + Vector3(cellHalfLength)
  val angle = applyTurns(input.turns)
  if (dice.getInt(1) == 0)
    listOf(newWallInternal(config, MeshId.diagonalWall.name, position, angle - Pi * 0.25f, biome))
  else
    listOf()
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
        randomDiagonalWall()
    )
