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
import silentorb.mythic.ent.scenery.ExpansionLibrary
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.ent.scenery.removeNodesAndChildren
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3i
import simulation.misc.BlockRotations
import simulation.misc.CellAttribute
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun getTraversable(cells: Map<Vector3i, BlockCell>) =
    cells
        .filterValues { it.isTraversable }
        .keys

fun rotateBlockBuilder(turns: Int, blockBuilder: BlockBuilder): BlockBuilder =
    if (turns == 0)
      blockBuilder
    else {
      val (block, builder) = blockBuilder
      val cells = block.cells.entries
          .associate { (offset, cell) ->
            val sides = rotateSides(turns)(cell.sides)
            rotateZ(turns, offset) to cell.copy(
                sides = sides,
            )
          }

      block.copy(
          cells = cells,
          traversable = getTraversable(cells),
          turns = turns
      ) to builder
    }

fun explodeBlockMap(blockBuilders: Collection<BlockBuilder>): List<BlockBuilder> {
  assert(blockBuilders.all { it.first.name.isNotEmpty() })
  return blockBuilders
      .flatMap { blockBuilder ->
        when (blockBuilder.first.rotations) {
          BlockRotations.none -> listOf(blockBuilder)
          BlockRotations.once -> listOf(blockBuilder, rotateBlockBuilder(1, blockBuilder))
          BlockRotations.all -> (0..3).mapNotNull { rotateBlockBuilder(it, blockBuilder) }
        }
      }
}

val defaultBiomeTextures: Map<String, Map<String, String>> = mapOf(
    Biomes.checkers to mapOf(
        PlaceholderTextures.floor to Textures.checkersBlackWhite,
        PlaceholderTextures.wall to Textures.checkersBlackWhite,
        PlaceholderTextures.ceiling to Textures.checkersBlackWhite,
    ),
    Biomes.dungeon to mapOf(
        PlaceholderTextures.floor to Textures.cobblestone,
        PlaceholderTextures.wall to Textures.bricks,
        PlaceholderTextures.ceiling to Textures.bricks,
    ),
    Biomes.forest to mapOf(
        PlaceholderTextures.floor to Textures.grass,
        PlaceholderTextures.wall to Textures.grass,
        PlaceholderTextures.ceiling to Textures.grass,
    ),
)

fun cellsFromSides(sides: List<Pair<CellDirection, Side?>>): Map<Vector3i, BlockCell> {
  val cells = sides
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

  val headroomCells = cells
      .filter { (cell, blockCell) ->
        !cells.containsKey(cell + Vector3i(0, 0, 1)) && blockCell.sides.any { it.value.height > 10 }
      }
      .keys
      .associate {
        val cell = it + Vector3i(0, 0, 1)
         cell to BlockCell(
             sides = mapOf(),
             isTraversable = false,
         )
      }

  return cells + headroomCells
}

fun prepareBlockGraph(graph: Graph, sideNodes: List<String>, biomes: Collection<String>): Graph {
  val truncatedGraph = graph.filter { !sideNodes.contains(it.source) }
  val defaultTexture = biomes
      .mapNotNull { defaultBiomeTextures[it] }
      .firstOrNull()

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

fun blockFromGraph(graph: Graph, cells: Map<Vector3i, BlockCell>, root: String, name: String,
                   biomes: Collection<String>,
                   heightOffset: Int): Block {
  val rotation = getNodeValue<BlockRotations>(graph, root, MarlothProperties.blockRotations)
  return Block(
      name = name + if (heightOffset != 0) heightOffset else "",
      cells = cells,
      traversable = getTraversable(cells),
      rotations = rotation ?: BlockRotations.none,
      biomes = biomes.toSet(),
      heightOffset = heightOffset,
  )
}

fun shouldOmit(cellDirections: List<CellDirection>, keys: Set<CellDirection>): Boolean =
    cellDirections.any { cellDirection ->
      keys.contains(cellDirection)
    }

fun builderFromGraph(graph: Graph, zShifts: Collection<CellDirection>): Builder {
  val showIfSideIsEmpty = filterByProperty(graph, MarlothProperties.showIfSideIsEmpty)
      .groupBy { it.source }

  return { input ->
    val omitted = showIfSideIsEmpty
        .mapValues { i ->
          i.value.map { entry ->
            val cellDirection = entry.target as CellDirection
            if (zShifts.contains(cellDirection))
              cellDirection.copy(cell = cellDirection.cell + Vector3i(0, 0, -1))
            else
              cellDirection
          }
        }
        .mapNotNull { (node, cellDirections) ->
          if (shouldOmit(cellDirections, input.neighbors.keys))
            node
          else
            null
        }

    removeNodesAndChildren(graph, omitted)
  }
}

fun adjustSideHeights(sides: List<Pair<CellDirection, Side?>>, height: Int): List<Pair<CellDirection, Side?>> =
    if (height == 0)
      sides
    else {
      sides
          .map { (cellDirection, side) ->
            if (side == null)
              cellDirection to side
            else {
              val lowerCellHeight = side.height + height < 0
              val nextCellDirection = if (lowerCellHeight)
                cellDirection.copy(
                    cell = cellDirection.cell + Vector3i(0, 0, -1)
                )
              else
                cellDirection

              val nextSide = side.copy(
                  height = (side.height + height + 100) % 100,
              )

              nextCellDirection to nextSide
            }
          }
    }

fun graphToBlockBuilder(name: String, graph: Graph): List<BlockBuilder> {
  val root = getGraphRoots(graph).first()
  val biomes = getGraphValues<String>(graph, root, MarlothProperties.biome)
  return if (biomes.none() || biomes.contains(Biomes.hedgeMaze))
    listOf()
  else {
    val heights = listOf(0) + getGraphValues(graph, root, MarlothProperties.heightVariant)
    val sideNodes = nodeAttributes(graph, GameAttributes.blockSide)
    val sides = gatherSides(sideGroups, graph, sideNodes)
    return heights.map { height ->
      val adjustedSides = adjustSideHeights(sides, height)
      val cells = cellsFromSides(adjustedSides)
      assert(cells.keys.contains(Vector3i.zero))
      val block = blockFromGraph(graph, cells, root, name, biomes, height)
      val finalGraph = prepareBlockGraph(graph, sideNodes, biomes)
      val offsets = sides
          .filter {
            val sideHeight = it.second?.height
            sideHeight != null && sideHeight + height < 0
          }
          .map { (cellDirection, _) ->
            cellDirection
          }
          .toSet()
      block to builderFromGraph(finalGraph, offsets)
    }
  }
}

fun graphsToBlockBuilders(graphLibrary: GraphLibrary): List<BlockBuilder> {
  val library = newExpansionLibrary(graphLibrary, mapOf())
  return graphLibrary
      .filterValues { graph ->
        graph.any { it.property == SceneProperties.type && it.target == GameAttributes.blockSide }
      }
      .keys
      .flatMap { key ->
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
  val expansionLibrary = ExpansionLibrary(graphs = graphLibrary)
  val graph = filterDistributionGroups(expansionLibrary, architectureSource)
  return Pair(blockGrid, graph)
}
