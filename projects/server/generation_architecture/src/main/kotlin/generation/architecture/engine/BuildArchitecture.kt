package generation.architecture.engine

import generation.general.*
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.LooseGraph
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

fun buildBlockCell(general: ArchitectureInput, block: Block, builder: Builder, position: Vector3i): LooseGraph {
  val neighbors = allDirections.filter { direction ->
    val rotated = rotateDirection(block.turns ?: 0)(direction)
    val offset = directionVectors[rotated]!!
    general.blockGrid.containsKey(position + offset)
  }.toSet()
  val input = BuilderInput(
      general = general,
      neighbors = neighbors
  )
  val result = builder(input) as LooseGraph
  val rotation = Quaternion().rotateZ((block.turns?.toFloat() ?: 0f) * quarterAngle)
  return result
//      .map(transformBlockHand(absoluteCellPosition(position), rotation))
}

fun buildArchitecture(general: ArchitectureInput, builders: Map<String, Builder>): LooseGraph {
  val groupedCellHands = general.blockGrid
      .mapValues { (position, block) ->
        val builder = builders[block.name] ?: throw Error("Could not find builder for block")
        buildBlockCell(general, block, builder, position)
      }

  return groupedCellHands.flatMap { it.value }
}
