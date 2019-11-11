package generation.elements

import generation.architecture.cellLength
import generation.misc.BiomeGrid
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import mythic.spatial.toVector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import kotlin.reflect.full.memberProperties

fun applyCellPosition(position: Vector3i): Vector3 =
    position.toVector3() * cellLength

fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
    grid.cells.mapValues { (cell, _) ->
      biomeGrid(applyCellPosition(cell))
    }

fun <T>enumerateMembers(polyominoes: Any): List<T> {
  return polyominoes.javaClass.kotlin.memberProperties.map { member ->
    @Suppress("UNCHECKED_CAST")
    member.get(polyominoes) as T
  }
}
