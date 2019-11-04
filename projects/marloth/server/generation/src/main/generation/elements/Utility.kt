package generation.elements

import generation.architecture.cellLength
import generation.misc.BiomeGrid
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import mythic.spatial.toVector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

class Direction {
  companion object {
    const val up = 0
    const val down = 1
    const val east = 2
    const val north = 3
    const val west = 4
    const val south = 5
  }
}

val verticalDirections: Map<Int, Vector3i> = mapOf(
    Direction.up to Vector3i(0, 0, 1),
    Direction.down to Vector3i(0, 0, -1)
)

val horizontalDirections: Map<Int, Vector3i> = mapOf(
    Direction.east to Vector3i(1, 0, 0),
    Direction.north to Vector3i(0, 1, 0),
    Direction.west to Vector3i(-1, 0, 0),
    Direction.south to Vector3i(0, -1, 0)
)

val sideDirections: Map<Int, Vector3i> = verticalDirections.plus(horizontalDirections)

fun getRotatedPolyominoes(polyominoes: Set<Polyomino>): Map<Polyomino, List<Polyomino>> {
  val nonFree = polyominoes.filter { it != rotatePolyomino(it, 1) }
  return nonFree.associateWith { polyomino ->
    (1..3)
        .map { turns -> rotatePolyomino(polyomino, turns) }
  }
}

fun addRotatedPolyominoes(polyominoes: Set<Polyomino>, rotatedPolyominoes: Map<Polyomino, Set<Polyomino>>) =
    polyominoes
        .plus(rotatedPolyominoes.flatMap { it.value })

fun applyCellPosition(position: Vector3i): Vector3 =
    position.toVector3() * cellLength

fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
    grid.cells.mapValues { (cell, _) ->
      biomeGrid(applyCellPosition(cell))
    }
