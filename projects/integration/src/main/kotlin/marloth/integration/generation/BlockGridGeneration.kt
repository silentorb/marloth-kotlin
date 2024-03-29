package marloth.integration.generation

import generation.architecture.biomes.Biomes
import generation.architecture.engine.*
import generation.architecture.engine.BlockBuilder
import generation.general.*
import marloth.clienting.editing.*
import marloth.definition.misc.getSideNodes
import marloth.definition.misc.isBlockSide
import marloth.definition.misc.nonTraversableBlockSides
import marloth.definition.misc.sideGroups
import marloth.scenery.enums.Textures
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.*
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3i
import simulation.misc.*

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
          name = block.name + "${-turns}turns",
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
        val nonNullSides = value
            .filter { it.second != null }
        val sideMap = nonNullSides
            .associate { it.first.direction to it.second!! }

        assert(nonNullSides.size == sideMap.size)

        val isTraversable = sideMap.any { it.value.isTraversable }
        val attributes = setOfNotNull(
            if (isTraversable) CellAttribute.isTraversable else null,
        )
        offset to BlockCell(
            sides = sideMap,
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

fun prepareBlockGraph(graph: Graph, sideNodes: Collection<String>, biomes: Collection<String>): Graph {
  val truncatedGraph = graph.filter { !sideNodes.contains(it.source) || !isSimpleSideNode(it.source) }
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

fun interpolateCellCount(cells: Set<Vector3i>, axis: Int): Int =
    if (cells.none())
      0
    else
      (cells.minOf { it[axis] } + 1 until cells.maxOf { it[axis] })
          .count { i -> cells.none { it[axis] == i } }

// Traversible cells are only marked along edges of a block
// For large blocks, this function pads the traversable cell count
// by including at least some middle cells in the count
fun interpolateTraversibleCellCount(cells: Set<Vector3i>): Int {
  val minimum =
      interpolateCellCount(cells, 0) +
          interpolateCellCount(cells, 1) +
          interpolateCellCount(cells, 2)
  return minimum * 3 / 2
}

fun blockFromGraph(graph: Graph, cells: Map<Vector3i, BlockCell>, root: String, name: String,
                   biomes: Collection<String>,
                   heightOffset: Int): Block {
  val rotation = getNodeValue<BlockRotations>(graph, root, GameProperties.blockRotations)
  val rarity = getNodeValue<Int>(graph, root, GameProperties.rarity)
  val traversable = getTraversable(cells)
  val blockAttributes = getNodesWithAttribute(graph, root).toSet()
  return Block(
      name = name + if (heightOffset != 0) heightOffset else "",
      cells = cells,
      traversable = traversable,
      rotations = rotation ?: BlockRotations.none,
      biomes = biomes.toSet(),
      heightOffset = heightOffset,
      significantCellCount = traversable.size + interpolateTraversibleCellCount(traversable),
      attributes = blockAttributes,
      rarity = if (rarity != null)
        Rarity.values()[rarity - 1]
      else
        Rarity.uncommon,
      isBiomeAdapter = nodeHasAttribute(graph, root, GameAttributes.biomeAdapter)
  )
}

fun shouldOmit(cellDirections: List<CellDirection>, keys: Set<CellDirection>): Boolean =
    cellDirections.any { cellDirection ->
      keys.contains(cellDirection)
    }

fun getVariableSideElements(property: String, attribute: String, graph: Graph, cellDirections: Map<CellDirection, CellDirection>) =
    filterByProperty(graph, property)
        .map { it.source to it.target as CellDirection }
        .plus(getNodesWithAttribute(graph, attribute)
            .mapNotNull {
              val direction = getNodeValue<CellDirection>(graph, it, GameProperties.direction)
              if (direction != null)
                it to direction
              else
                null
            }
        )
        .groupBy { it.first }
        .mapValues { i ->
          i.value.map { cellDirections[it.second] ?: it.second }
        }

fun builderFromGraph(graph: Graph, cellDirections: Map<CellDirection, CellDirection>, height: Int): Builder {
  val showIfSideIsEmpty =
      getVariableSideElements(GameProperties.showIfSideIsEmpty, GameAttributes.showIfSideIsEmpty, graph, cellDirections)

  val hideIfSideIsEmpty =
      getVariableSideElements(GameProperties.hideIfSideIsEmpty, GameAttributes.hideIfSideIsEmpty, graph, cellDirections)

  val showIfHeightOffset =
      if (height == 0)
        getNodesWithAttribute(graph, GameAttributes.showIfHeightOffset)
      else
        listOf()

  return { input ->
    val omitted = showIfSideIsEmpty
        .filter { shouldOmit(it.value, input.neighbors.keys) }
        .plus(
            hideIfSideIsEmpty
                .filter { !shouldOmit(it.value, input.neighbors.keys) }
        )
        .map { it.key }
        .plus(showIfHeightOffset)
        .distinct()

    removeNodesAndChildren(graph, omitted)
  }
}

fun mapSideHeightAdjustments(sides: List<Pair<CellDirection, Side?>>, height: Int): Map<CellDirection, CellDirection> =
    sides
        .filter { it.second != null }
        .associate { (cellDirection, side) ->
          val newHeight = side!!.height + height
          val nextCellDirection = when {
            newHeight < 0 -> cellDirection.copy(
                cell = cellDirection.cell + Vector3i(0, 0, -1)
            )
            newHeight >= cellHeightResolution -> cellDirection.copy(
                cell = cellDirection.cell + Vector3i(0, 0, 1)
            )
            else -> cellDirection
          }
          cellDirection to nextCellDirection
        }

fun adjustSideHeights(sides: List<Pair<CellDirection, Side?>>, cellDirections: Map<CellDirection, CellDirection>,
                      height: Int): List<Pair<CellDirection, Side?>> =
    if (height == 0)
      sides
    else {
      sides
          .map { (cellDirection, side) ->
            if (side == null)
              cellDirection to side
            else {
              val newHeight = side.height + height
              val nextCellDirection = cellDirections[cellDirection] ?: cellDirection
              val nextSide = side.copy(
                  height = (newHeight + cellHeightResolution) % cellHeightResolution,
              )

              nextCellDirection to nextSide
            }
          }
    }

fun graphToBlockBuilder(name: String, graph: Graph): List<BlockBuilder> {
  val root = getGraphRoots(graph).first()
  val biomes = getNodeValues<String>(graph, root, GameProperties.biome)
  return if (biomes.none() || biomes.contains(Biomes.hedgeMaze))
    listOf()
  else {
    val heights = listOf(0) + getNodeValues(graph, root, GameProperties.heightVariant)
    val sideNodes = getSideNodes(graph)

    val sides = gatherSides(sideGroups, graph, sideNodes, nonTraversableBlockSides)
    return heights.map { height ->
      val cellDirectionsMap = mapSideHeightAdjustments(sides, height)
      val adjustedSides = adjustSideHeights(sides, cellDirectionsMap, height)
      val cells = cellsFromSides(adjustedSides)
      assert(cells.keys.contains(Vector3i.zero))
      val block = blockFromGraph(graph, cells, root, name, biomes, height)
      val finalGraph = prepareBlockGraph(graph, sideNodes, biomes)
      block to builderFromGraph(finalGraph, cellDirectionsMap, height)
    }
  }
}

fun graphsToBlockBuilders(graphLibrary: GraphLibrary): List<BlockBuilder> {
  val library = newExpansionLibrary(graphLibrary, mapOf())
  return graphLibrary
      .filterValues { graph ->
        graph.any(::isBlockSide)
      }
      .keys
      .flatMap { key ->
        val expanded = applyCellDirectionOffsets(expandGameInstances(library, key))
        graphToBlockBuilder(key, expanded)
      }
}

fun generateWorldBlocks(dice: Dice, generationConfig: GenerationConfig,
                        expansionLibrary: ExpansionLibrary): Pair<BlockGrid, Graph> {
  val coreBlocks = graphsToBlockBuilders(expansionLibrary.graphs)
  val blockBuilders = explodeBlockMap(coreBlocks)
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
  val home = blocks.firstOrNull { it.name == "home-set" }
  if (home == null)
    throw Error("Could not find home-set block")

  val blockGrid = newBlockGrid(generationConfig.seed, dice, home, blocks - home, generationConfig.cellCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val graph = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, graph)
}
