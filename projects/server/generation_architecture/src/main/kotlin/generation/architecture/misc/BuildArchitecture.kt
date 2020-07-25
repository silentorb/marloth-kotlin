package generation.architecture.misc

import generation.general.Block
import generation.general.directionVectors
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.absoluteCellPosition
import simulation.misc.cellHalfLength

fun transformBlockHand(block: Block, position: Vector3i) = { hand: Hand ->
  val body = hand.body
  if (body == null)
    hand
  else {
    val rotation = Quaternion().rotateZ(block.turns.toFloat() * Pi / 0.5f)
    hand.copy(
        body = body.copy(
            position = rotation.transform(body.position) + cellHalfLength + absoluteCellPosition(position),
            orientation = body.orientation * rotation
        )
    )
  }
}

fun buildBlockCell(general: ArchitectureInput, block: Block, builder: Builder, position: Vector3i): List<Hand> {
  val biomeName = general.cellBiomes[position]!!
  val input = BuilderInput(
      general = general,
      biome = general.config.biomes[biomeName]!!,
      isNeighborPopulated = directionVectors.mapValues { (_, offset) ->
        general.cellBiomes.containsKey(position + offset)
      }
  )
  val result = builder(input)
  return result
      .map(transformBlockHand(block, position))
}

fun buildArchitecture(general: ArchitectureInput, builders: Map<String, Builder>): List<Hand> {
  val groupedCellHands = general.blockGrid
      .mapValues { (position, block) ->
        val builder = builders[block.name] ?: throw Error("Could not find builder for block")
        buildBlockCell(general, block, builder, position)
      }

  return groupedCellHands.flatMap { it.value }
}

fun mapArchitectureCells(elementMap: Map<Vector3i, List<IdHand>>): Map<Id, Vector3i> =
    elementMap
        .flatMap { (key, value) -> value.map { Pair(it.id, key) } }
        .associate { it }
