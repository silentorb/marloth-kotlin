package marloth.integration.misc

import generation.architecture.engine.*
import generation.architecture.matrical.BlockBuilder
import generation.general.*
import marloth.clienting.editing.expandGameInstances
import marloth.clienting.editing.newExpansionLibrary
import marloth.definition.misc.traversibleBlockSides
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.filterByAttribute
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.hasAttribute
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import simulation.misc.CellAttribute
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun explodeBlockMap(blockBuilders: Collection<BlockBuilder>): List<BlockBuilder> {
  assert(blockBuilders.all { it.first.name.isNotEmpty() })
  val (needsRotatedVariations, noTurns) = blockBuilders
      .partition { (block, _) ->
        !block.lockedRotation
      }

  val rotated = needsRotatedVariations
      .flatMap { (block, builder) ->
        (0..3)
            .mapNotNull { turns ->
              val cells = block.cells.entries
                  .associate { (offset, cell) ->
                    val sides = rotateSides(turns)(cell.sides)
                    rotateZ(turns, offset) to cell.copy(
                        sides = sides,
                    )
                  }
              if (cells == block.cells)
                null
              else
                block.copy(
                    cells = cells,
                    turns = turns
                ) to builder
            }
      }

  return noTurns + rotated
}

val directionMap: Map<String, Direction> = Direction.values().associateBy { it.name }

fun graphToBlockBuilder(name: String, graph: Graph): BlockBuilder {
  val root = getGraphRoots(graph).first()
  val myDefaultBiome = getGraphValue<String>(graph, root, MarlothProperties.myBiome)
  val otherDefaultBiome = getGraphValue<String>(graph, root, MarlothProperties.otherBiome)
  val sideNodes = filterByAttribute(graph, GameAttributes.blockSide)
  val cells = sideNodes
      .mapNotNull { node ->
        val mine = getGraphValue<String>(graph, node, MarlothProperties.mine)
        val other = getGraphValue<String>(graph, node, MarlothProperties.other)
        val cellDirection = getGraphValue<CellDirection>(graph, node, MarlothProperties.direction)
        if (mine == null || other == null || cellDirection == null)
          null
        else {
          val height = getGraphValue<Int>(graph, node, MarlothProperties.sideHeight) ?: StandardHeights.first
          val myBiome = getGraphValue<String>(graph, node, MarlothProperties.myBiome)
          val otherBiome = getGraphValue<String>(graph, node, MarlothProperties.otherBiome)

          cellDirection to Side(
              mine = ConnectionContract(mine, biome = myBiome ?: myDefaultBiome),
              other = ConnectionContract(other, biome = otherBiome ?: otherDefaultBiome),
              height = height,
          )
        }
      }
      .groupBy { it.first }
      .entries
      .associate { (offset, value) ->
        val sides = value.associate { it.first.direction to it.second }
        val isTraversible = sides.any { traversibleBlockSides.contains(it.value.mine.type) }
        val attributes = setOfNotNull(
            if (isTraversible) CellAttribute.isTraversable else null,
        )
        offset.cell to BlockCell(
            sides = sides,
            attributes = attributes,
        )
      }

  val lockedRotation = hasAttribute(graph, root, GameAttributes.lockedRotation)
  val showIfSideIsEmpty = filterByProperty(graph, MarlothProperties.showIfSideIsEmpty)

  val block = Block(
      name = name,
      cells = cells,
      lockedRotation = lockedRotation,
  )
  val truncatedGraph = graph.filter { !sideNodes.contains(it.source) }

  val builder: Builder = { input ->
    val omitted = showIfSideIsEmpty
        .mapNotNull { entry ->
          val cellDirection = entry.target as CellDirection
          if (input.neighbors.keys.contains(cellDirection.direction))
            entry.source
          else
            null
        }

    truncatedGraph.filter { !omitted.contains(it.source) }
  }
  return block to builder
}

fun graphsToBlockBuilders(graphLibrary: GraphLibrary): List<BlockBuilder> {
  val library = newExpansionLibrary(graphLibrary, mapOf())
  return graphLibrary
      .filterValues { graph ->
        graph.any { it.property == SceneProperties.type && it.target == GameAttributes.blockSide }
      }
      .keys
      .map { key ->
        val expanded = expandGameInstances(library, key)
        graphToBlockBuilder(key, expanded)
      }
}

fun generateWorldBlocks(dice: Dice, generationConfig: GenerationConfig,
                        graphLibrary: GraphLibrary): Pair<BlockGrid, LooseGraph> {
  val blockBuilders = explodeBlockMap(graphsToBlockBuilders(graphLibrary))
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
  val home = blocks.first { it.name == "home-set" }
  val blockGrid = newBlockGrid(dice, home, blocks - home, generationConfig.roomCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, architectureSource)
}
