package generation.elements

import generation.architecture.cellLength
import generation.misc.BiomeGrid
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import mythic.spatial.toVector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

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
