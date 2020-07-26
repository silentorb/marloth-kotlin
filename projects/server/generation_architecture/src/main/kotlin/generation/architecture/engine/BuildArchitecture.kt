package generation.architecture.engine

import generation.architecture.misc.ArchitectureInput
import generation.architecture.misc.Builder
import generation.architecture.misc.BuilderInput
import generation.general.*
import silentorb.mythic.spatial.*
import simulation.main.Hand
import simulation.misc.absoluteCellPosition
import simulation.misc.cellHalfLength

fun transformBlockHand(position: Vector3, rotation: Quaternion) = { hand: Hand ->
  val body = hand.body
  if (body == null)
    hand
  else {
    hand.copy(
        body = body.copy(
            position = position + rotation.transform(body.position),
            orientation = rotation * body.orientation
        )
    )
  }
}

fun buildBlockCell(general: ArchitectureInput, block: Block, builder: Builder, position: Vector3i): List<Hand> {
  val biomeName = general.cellBiomes[position]!!
  val neighbors = allDirections.filter { direction ->
    val rotated = rotateDirection(block.turns)(direction)
    val offset = directionVectors[rotated]!!
    general.cellBiomes.containsKey(position + offset)
  }.toSet()
  val input = BuilderInput(
      general = general,
      biome = general.config.biomes[biomeName]!!,
      neighbors = neighbors
  )
  val result = builder(input)
  val rotation = Quaternion().rotateZ(block.turns.toFloat() * quarterAngle)
  return result
      .map(transformBlockHand(absoluteCellPosition(position) + cellHalfLength, rotation))
}

fun buildArchitecture(general: ArchitectureInput, builders: Map<String, Builder>): List<Hand> {
  val groupedCellHands = general.blockGrid
      .mapValues { (position, block) ->
        val builder = builders[block.name] ?: throw Error("Could not find builder for block")
        buildBlockCell(general, block, builder, position)
      }

  return groupedCellHands.flatMap { it.value }
}