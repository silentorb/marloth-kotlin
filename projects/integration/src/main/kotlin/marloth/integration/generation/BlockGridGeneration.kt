package marloth.integration.generation

import generation.architecture.biomes.Biomes
import generation.architecture.engine.*
import generation.architecture.matrical.BlockBuilder
import generation.general.*
import marloth.clienting.editing.PlaceholderTextures
import marloth.clienting.editing.expandGameInstances
import marloth.clienting.editing.newExpansionLibrary
import marloth.definition.misc.sideGroups
import marloth.definition.misc.traversableBlockSides
import marloth.scenery.enums.Textures
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.hasAttribute
import silentorb.mythic.ent.scenery.removeNodesAndChildren
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3i
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
              if (turns != 0 && cells == block.cells)
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

val defaultBiomeTextures: Map<String, Map<String, String>> = mapOf(
    Biomes.checkers to mapOf(
        PlaceholderTextures.floor to Textures.checkersBlackWhite,
        PlaceholderTextures.wall to Textures.checkersBlackWhite,
        PlaceholderTextures.ceiling to Textures.checkersBlackWhite,
    ),
    Biomes.forest to mapOf(
        PlaceholderTextures.floor to Textures.grass,
        PlaceholderTextures.wall to Textures.grass,
        PlaceholderTextures.ceiling to Textures.grass,
    ),
)

tailrec fun expandSideGroups(sideGroups: Map<String, Set<String>>, value: Collection<String>, step: Int = 0): Collection<String> {
  if (step > 20)
    throw Error("Infinite loop detected with expanding side type groups")

  val groups = sideGroups.keys.intersect(value)
  return if (groups.none())
    value
  else {
    val next = (value - groups) + groups.flatMap { sideGroups[it]!! }
    expandSideGroups(sideGroups, next, step + 1)
  }
}

fun gatherSides(graph: Graph, sideNodes: List<String>) =
    sideNodes
        .mapNotNull { node ->
          val mine = getGraphValue<String>(graph, node, MarlothProperties.mine)
          val initialOther = getGraphValues<String>(graph, node, MarlothProperties.other)
          val other = expandSideGroups(sideGroups, initialOther)
          val cellDirection = getGraphValue<CellDirection>(graph, node, MarlothProperties.direction)
          if (cellDirection == null)
            null
          else if (mine == null || other.none())
            cellDirection to null
          else {
            val height = getGraphValue<Int>(graph, node, MarlothProperties.sideHeight) ?: StandardHeights.first
            cellDirection to Side(
                mine = mine,
                other = other.toSet(),
                height = height,
            )
          }
        }

fun cellsFromSides(allSides: List<Pair<CellDirection, Side?>>) =
    allSides
        .groupBy { it.first.cell }
        .entries
        .associate { (offset, value) ->
          // Null sides are used to indicate the existence of a non-traversable cell
          val sides = value
              .filter { it.second != null }
              .associate { it.first.direction to it.second!! }

          val isTraversable = sides.any { traversableBlockSides.contains(it.value.mine) }
          val attributes = setOfNotNull(
              if (isTraversable) CellAttribute.isTraversable else null,
          )
          offset to BlockCell(
              sides = sides,
              isTraversable = isTraversable,
              attributes = attributes,
          )
        }

fun prepareBlockGraph(graph: Graph, sideNodes: List<String>, biome: String): Graph {
  val truncatedGraph = graph.filter { !sideNodes.contains(it.source) }
  val defaultTexture = defaultBiomeTextures[biome]

  return if (defaultTexture == null)
    truncatedGraph
  else {
    val placeholderEntries = graph.filter { entry ->
      entry.property == SceneProperties.texture && defaultTexture.containsKey(entry.target)
    }

    val replacements = placeholderEntries
        .map { entry -> entry.copy(target = defaultTexture[entry.target]!!) }

    truncatedGraph - placeholderEntries + replacements
  }
}

fun blockFromGraph(graph: Graph, cells: Map<Vector3i, BlockCell>, root: String, name: String, biome: String): Block {
  val traversable = cells
      .filterValues { it.isTraversable }
      .keys

  val lockedRotation = hasAttribute(graph, root, GameAttributes.lockedRotation)
  return Block(
      name = name,
      cells = cells,
      traversable = traversable,
      lockedRotation = lockedRotation,
      biome = biome,
  )
}

fun builderFromGraph(graph: Graph): Builder {
  val showIfSideIsEmpty = filterByProperty(graph, MarlothProperties.showIfSideIsEmpty)
      .groupBy { it.source }
      .mapValues { i -> i.value.map { it.target as CellDirection } }

  return { input ->
    val omitted = showIfSideIsEmpty
        .mapNotNull { (node, cellDirections) ->
          val shouldOmit = cellDirections.any { cellDirection ->
            input.neighbors.keys.contains(cellDirection)
          }
          if (shouldOmit)
            node
          else
            null
        }

    removeNodesAndChildren(graph, omitted)
  }
}

fun graphToBlockBuilder(name: String, graph: Graph): BlockBuilder? {
  val root = getGraphRoots(graph).first()
  val biome = getGraphValue<String>(graph, root, MarlothProperties.biome)
  return if (biome == null)
    null
  else {
    val sideNodes = nodeAttributes(graph, GameAttributes.blockSide)
    val sides = gatherSides(graph, sideNodes)
    val cells = cellsFromSides(sides)
    val block = blockFromGraph(graph, cells, root, name, biome)
    val finalGraph = prepareBlockGraph(graph, sideNodes, biome)
    val builder: Builder = builderFromGraph(finalGraph)
    return block to builder
  }
}

fun graphsToBlockBuilders(graphLibrary: GraphLibrary): List<BlockBuilder> {
  val library = newExpansionLibrary(graphLibrary, mapOf())
  return graphLibrary
      .filterValues { graph ->
        graph.any { it.property == SceneProperties.type && it.target == GameAttributes.blockSide }
      }
      .keys
      .mapNotNull { key ->
        val expanded = expandGameInstances(library, key)
        graphToBlockBuilder(key, expanded)
      }
}

fun generateWorldBlocks(dice: Dice, generationConfig: GenerationConfig,
                        graphLibrary: GraphLibrary): Pair<BlockGrid, Graph> {
  val coreBlocks = graphsToBlockBuilders(graphLibrary)
  val blockBuilders = explodeBlockMap(coreBlocks)
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
  val home = blocks.firstOrNull { it.name == "home-set" }
  if (home == null)
    throw Error("Could not find home-set block")

  val blockGrid = newBlockGrid(generationConfig.seed, dice, home, blocks - home, generationConfig.cellCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  val graph = filterDistributionGroups(architectureSource)
  return Pair(blockGrid, graph)
}
