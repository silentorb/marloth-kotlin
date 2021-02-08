package generation.architecture.engine

import generation.general.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.*
import simulation.main.Hand
import simulation.misc.absoluteCellPosition

fun gatherNeighbors(grid: BlockGrid, block: Block, position: Vector3i): Map<CellDirection, String> =
    block.cells.keys
        .flatMap { cell ->
          val cellOffset = position + cell
          allDirections
              .mapNotNull { direction ->
                val rotated = rotateDirection(block.turns)(direction)
                val offset = directionVectors[rotated]!!
                val other = grid[cellOffset + offset]
                val reverse = oppositeDirections[rotated]
                val side = other?.cell?.sides?.getOrDefault(reverse, null)
                val contract = side?.mine
                if (contract != null)
                  CellDirection(cell, direction) to contract
                else
                  null
              }
        }
        .associate { it }

fun transformBlockOutput(block: Block, position: Vector3i, graph: Graph): Graph {
  val zRotation = (block.turns.toFloat()) * quarterAngle
  val rotation = Vector3(0f, 0f, zRotation)
  val location = absoluteCellPosition(position)
  val roots = getGraphRoots(graph)
  val rootTransforms = roots.flatMap { root ->
    val transform = getNodeTransform(graph, root)
    listOf(
        Entry(root, SceneProperties.translation, transform.translation() + location),
        Entry(root, SceneProperties.rotation, transform.rotation() + rotation),
    )
  }
  return replaceValues(graph, rootTransforms)
}

fun buildBlockCell(general: ArchitectureInput, block: Block, builder: Builder, location: Vector3i): Graph {
  val grid = general.blockGrid
  val neighbors = gatherNeighbors(grid, block, location)
  val input = BuilderInput(
      general = general,
      neighborOld = setOf(),
      neighbors = neighbors,
  )
  val graph = builder(input) as Graph
  val result = transformBlockOutput(block, location, graph)
  val k = result.filter {it.property == SceneProperties.mesh}
  return result
}

fun buildArchitecture(general: ArchitectureInput, builders: Map<String, Builder>): Graph {
  val groupedCellHands = general.blockGrid
      .filterValues { it.offset == Vector3i.zero }
      .mapValues { (position, block) ->
        val builder = builders[block.source.name] ?: throw Error("Could not find builder for block")
        buildBlockCell(general, block.source, builder, position)
      }

  val merged = if (groupedCellHands.any())
    groupedCellHands.values.reduce { a, b -> mergeGraphsWithRenaming(a, b) }
  else
    listOf()

  return merged
}
