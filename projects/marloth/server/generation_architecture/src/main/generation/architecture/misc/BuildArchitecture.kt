package generation.architecture.misc

import generation.general.Block
import generation.general.BlockMap
import generation.general.allDirections
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.absoluteCellPosition

fun buildArchitecture(general: ArchitectureInput,
                      blockMap: BlockMap,
                      builders: Map<Block, Builder>): Map<Vector3i, List<Hand>> {
  return general.blockGrid.mapValues { (position, block) ->
    val info = blockMap[block]!!
    val biomeName = general.cellBiomes[position]!!
    val builder = builders[info.original]
    if (builder == null)
      throw Error("Could not find builder for block")

    val input = BuilderInput(
        general = general,
        position = absoluteCellPosition(position),
        turns = info.turns,
        cell = position,
        biome = general.config.biomes[biomeName]!!,
        sides = allDirections.associateWith(general.getUsableCellSide(position))
    )
    builder(input)
  }
}

fun mapArchitectureCells(elementMap: Map<Vector3i, List<IdHand>>): Map<Id, Vector3i> =
    elementMap
        .flatMap { (key, value) -> value.map { Pair(it.id, key) } }
        .associate { it }
