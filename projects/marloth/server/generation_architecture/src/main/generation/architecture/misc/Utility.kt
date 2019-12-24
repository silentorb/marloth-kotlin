package generation.architecture.misc

import simulation.misc.cellLength
import generation.general.BiomeGrid
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.absoluteCellPosition
import kotlin.reflect.full.memberProperties

fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
    grid.cells.mapValues { (cell, _) ->
      biomeGrid(absoluteCellPosition(cell))
    }

fun <T> enumerateMembers(container: Any): List<T> {
  return container.javaClass.kotlin.memberProperties.map { member ->
    @Suppress("UNCHECKED_CAST")
    member.get(container) as T
  }
}

fun <T> getMember(container: Any, name: String): T {
  val property = container.javaClass.kotlin.memberProperties.first { it.name == name }
  @Suppress("UNCHECKED_CAST")
  return property.get(container) as T
}
