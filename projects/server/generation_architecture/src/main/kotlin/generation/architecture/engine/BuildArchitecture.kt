package generation.architecture.engine

import generation.general.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.*
import simulation.main.Hand
import simulation.misc.absoluteCellPosition

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
  val neighborsOld = allDirections.filter { direction ->
    val rotated = rotateDirection(block.turns ?: 0)(direction)
    val offset = directionVectors[rotated]!!
    general.blockGrid.containsKey(position + offset)
  }.toSet()
  val neighbors = allDirections
      .mapNotNull { direction ->
        val rotated = rotateDirection(block.turns ?: 0)(direction)
        val offset = directionVectors[rotated]!!
        val other = general.blockGrid[position + offset]
        val reverse = oppositeDirections[direction]
        val side = other?.sides?.getOrDefault(reverse, null)
        val contract = side?.mine
        if (contract != null)
          direction to contract
        else
          null
      }
      .associate { it }

  val input = BuilderInput(
      general = general,
      neighborOld = neighborsOld,
      neighbors = neighbors,
  )
  val result = builder(input) as LooseGraph
  val zRotation = (block.turns?.toFloat() ?: 0f) * quarterAngle
  val rotation = Vector3(0f, 0f, zRotation)
  val location = absoluteCellPosition(position)
  val roots = getGraphRoots(result)
  val rootTransforms = roots.flatMap { root ->
    val transform = getNodeTransform(result, root)
    listOf(
        Entry(root, SceneProperties.translation, transform.translation() + location),
        Entry(root, SceneProperties.rotation, transform.rotation() + rotation),
    )
  }
  return replaceValues(result, rootTransforms)
}

fun buildArchitecture(general: ArchitectureInput, builders: Map<String, Builder>): LooseGraph {
  val groupedCellHands = general.blockGrid
      .mapValues { (position, block) ->
        val builder = builders[block.name] ?: throw Error("Could not find builder for block")
        buildBlockCell(general, block, builder, position)
      }

  return groupedCellHands.values.reduce { a, b -> mergeGraphsWithRenaming(a, b) }
}
