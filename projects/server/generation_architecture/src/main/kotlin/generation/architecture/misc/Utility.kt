package generation.architecture.misc

import generation.architecture.building.BlockBuilder
import generation.general.BiomeGrid
import generation.general.Block
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.absoluteCellPosition
import simulation.misc.cellLength

//import kotlin.reflect.full.memberProperties

fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
    grid.cells.mapValues { (cell, _) ->
      biomeGrid(absoluteCellPosition(cell))
    }

//fun <T> enumerateMembers(container: Any): List<T> {
//  return container.javaClass.kotlin.memberProperties.map { member ->
//    @Suppress("UNCHECKED_CAST")
//    member.get(container) as T
//  }
//}
//
//fun <T> getMember(container: Any, name: String): T {
//  val property = container.javaClass.kotlin.memberProperties.first { it.name == name }
//  @Suppress("UNCHECKED_CAST")
//  return property.get(container) as T
//}

fun splitBlockBuilders(blockBuilders: Collection<BlockBuilder>): Pair<Set<Block>, Map<Block, Builder>> =
    Pair(
        blockBuilders.map { it.block }.toSet(),
        blockBuilders.associate { Pair(it.block, it.builder ?: { listOf() }) }
    )

fun squareOffsets(length: Int): List<Vector3> {
  val step = cellLength / length
  val start = step / 2f

  return (0 until length).flatMap { y ->
    (0 until length).map { x ->
      Vector3(start + step * x, start + step * y, 1f)
    }
  }
}