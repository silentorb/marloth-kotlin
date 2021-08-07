package generation.architecture.engine

import generation.general.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
import silentorb.mythic.ent.scenery.integrateTransform
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.*
import simulation.misc.absoluteCellPosition
import simulation.misc.cellLength

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
                if (contract != null) {
                  val rotatedCell = rotateZ(-block.turns, cell)
                  CellDirection(rotatedCell, direction) to contract
                } else
                  null
              }
        }
        .associate { it }

fun transformBlockOutput(block: Block, position: Vector3i, graph: Graph): Graph {
  val zRotation = (block.turns.toFloat()) * quarterAngle
  val rotation = Vector3(0f, 0f, zRotation)
  val location = absoluteCellPosition(position) + Vector3(0f, 0f, block.heightOffset.toFloat() / 100f * cellLength)
  val parentTransform = integrateTransform(location, rotation)
  val roots = getGraphRoots(graph)
  val rootTransforms = roots.flatMap { root ->
    val localTransform = getAbsoluteNodeTransform(graph, root)
    listOf(
        Entry(root, SceneProperties.transform, parentTransform * localTransform),
    )
  }
  return replaceValues(graph, rootTransforms)
}

fun buildBlockCell(general: ArchitectureInput, block: Block, builder: Builder, location: Vector3i): Graph {
  val grid = general.blockGrid
  val neighbors = gatherNeighbors(grid, block, location)
  val input = BuilderInput(
      general = general,
      neighbors = neighbors,
      turns = block.turns,
      height = block.heightOffset,
  )
  val graph = HashedList.from(builder(input) as Graph)
  val result = transformBlockOutput(block, location, graph)
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
