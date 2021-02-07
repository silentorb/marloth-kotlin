package marloth.integration.misc

import generation.architecture.biomes.Biomes
import generation.architecture.engine.*
import generation.architecture.matrical.BlockBuilder
import generation.general.*
import marloth.clienting.editing.expandGameInstances
import marloth.clienting.editing.newExpansionLibrary
import marloth.definition.misc.traversableBlockSides
import marloth.scenery.enums.Textures
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.nodeAttributes
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

val directionMap: Map<String, Direction> = Direction.values().associateBy { it.name }

val defaultBiomeTextures: Map<String, Map<String, String>> = mapOf(
    Biomes.checkers to mapOf(
        "default" to Textures.checkersBlackWhite,
    ),
    Biomes.forest to mapOf(
        "default" to Textures.grass,
    ),
)

fun graphToBlockBuilder(name: String, graph: Graph): BlockBuilder? {
  val root = getGraphRoots(graph).first()
  val biome = getGraphValue<String>(graph, root, MarlothProperties.biome)
  return if (biome == null)
    null
  else {
    val sideNodes = nodeAttributes(graph, GameAttributes.blockSide)
    val allSides = sideNodes
        .mapNotNull { node ->
          val mine = getGraphValue<String>(graph, node, MarlothProperties.mine)
          val other = getGraphValue<String>(graph, node, MarlothProperties.other)
          val cellDirection = getGraphValue<CellDirection>(graph, node, MarlothProperties.direction)
          if (cellDirection == null)
            null
          else if (mine == null || other == null)
            cellDirection to null
          else {
            val height = getGraphValue<Int>(graph, node, MarlothProperties.sideHeight) ?: StandardHeights.first
            cellDirection to Side(
                mine = mine,
                other = other,
                height = height,
            )
          }
        }

    val cells = allSides
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

    val traversable = cells
        .filterValues { it.isTraversable }
        .keys

    val lockedRotation = hasAttribute(graph, root, GameAttributes.lockedRotation)
    val showIfSideIsEmpty = filterByProperty(graph, MarlothProperties.showIfSideIsEmpty)
        .groupBy { it.source }
        .mapValues { it.value.map { it.target as CellDirection } }

    val block = Block(
        name = name,
        cells = cells,
        traversable = traversable,
        lockedRotation = lockedRotation,
        biome = biome,
    )
    val truncatedGraph = graph.filter { !sideNodes.contains(it.source) }

    val texturelessMeshes = truncatedGraph
        .filter { entry ->
          entry.property == SceneProperties.mesh && truncatedGraph.none {
            it.source == entry.source && it.property == SceneProperties.texture
          }
        }
        .map { it.source }

    val defaultTexture = defaultBiomeTextures[biome]?.getOrDefault("default", null)
    val textureAdditions =
        if (defaultTexture != null) {
          texturelessMeshes
              .map { Entry(it, SceneProperties.texture, defaultTexture) }
        }
    else
          listOf()
    
    val finalGraph = truncatedGraph + textureAdditions

    val builder: Builder = { input ->
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

      finalGraph.filter { !omitted.contains(it.source) }
    }
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
                        graphLibrary: GraphLibrary): Pair<BlockGrid, LooseGraph> {
  val coreBlocks = graphsToBlockBuilders(graphLibrary)
  val blockBuilders = explodeBlockMap(coreBlocks)
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
  val home = blocks.firstOrNull { it.name == "home-set" }
  if (home == null)
    throw Error("Could not find home-set block")

  val blockGrid = newBlockGrid(dice, home, blocks - home, generationConfig.roomCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, architectureSource)
}
