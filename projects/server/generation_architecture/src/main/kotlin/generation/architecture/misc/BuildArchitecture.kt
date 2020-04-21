package generation.architecture.misc

import generation.architecture.boundaries.buildBoundaries
import generation.architecture.definition.homeBlock
import generation.architecture.definition.newHorizontalBoundaryBuilders
import generation.architecture.definition.newVerticalBoundaryBuilders
import generation.general.*
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.absoluteCellPosition

fun buildArchitecture(general: ArchitectureInput,
                      builders: Map<Block, Builder>): List<Hand> {

  val groupedBoundaryHands = buildBoundaries(general,
      horizontalBuilders = newHorizontalBoundaryBuilders(),
      verticalBuilders = newVerticalBoundaryBuilders()
  )

  val groupedCellHands = general.blockGrid.mapValues { (position, block) ->
    val biomeName = general.cellBiomes[position]!!
    val original = if (block.turns != 0) block.copy(turns = 0) else block
    val builder = builders[original]
    if (builder == null)
      throw Error("Could not find builder for block")

    val boundaryHands = groupedBoundaryHands
        .filterKeys { (first, second) ->
          position == first || position == second
        }
        .flatMap { (key, value) ->
          val neighor = if (position == key.first) key.second else key.first
          val direction = directionVectorsReverseLookup[neighor - position]!!
          value.map { Pair(direction, it) }
        }
        .groupBy({ it.first }) { it.second }

    val input = BuilderInput(
        general = general,
        position = absoluteCellPosition(position),
        turns = block.turns,
        cell = position,
        biome = general.config.biomes[biomeName]!!,
        sides = allDirections.associateWith(getUsableCellSide(general.gridSideMap)(position)),
        boundaryHands = boundaryHands
    )
    builder(input)
  }

  return groupedBoundaryHands.flatMap { it.value }
      .plus(groupedCellHands.flatMap { it.value })
}

fun mapArchitectureCells(elementMap: Map<Vector3i, List<IdHand>>): Map<Id, Vector3i> =
    elementMap
        .flatMap { (key, value) -> value.map { Pair(it.id, key) } }
        .associate { it }
