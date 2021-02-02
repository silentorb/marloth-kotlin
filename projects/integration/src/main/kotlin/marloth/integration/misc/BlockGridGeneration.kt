package marloth.integration.misc

import generation.architecture.engine.*
import generation.architecture.matrical.BlockBuilder
import generation.general.*
import marloth.clienting.editing.expandGameInstances
import marloth.clienting.editing.newExpansionLibrary
import marloth.definition.misc.traversibleBlockSides
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.filterByAttribute
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Quaternion
import simulation.misc.CellAttribute
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun explodeBlockMap(blockBuilders: Collection<BlockBuilder>): List<BlockBuilder> {
  assert(blockBuilders.all { it.first.name.isNotEmpty() })
  val (needsRotatedVariations, noTurns) = blockBuilders
      .partition { (block, _) ->
        !block.attributes.contains(CellAttribute.lockedRotation) &&
            block.sides != rotateSides(1)(block.sides)
      }

  val rotated = needsRotatedVariations
      .flatMap { (block, builder) ->
        (0..3)
            .map { turns ->
              val rotation = Quaternion().rotateZ(applyTurns(turns))
              block.copy(
                  sides = rotateSides(turns)(block.sides),
                  slots = block.slots.map { slot ->
                    rotation.transform(slot)
                  },
                  turns = turns
              ) to builder
            }
      }

  return noTurns + rotated
}

val directionMap: Map<String, Direction> = Direction.values().associate { it.name to it }

fun graphToBlockBuilder(name: String, graph: Graph): BlockBuilder {
  val sideNodes = filterByAttribute(graph, GameAttributes.blockSide)
  val sides = sideNodes
      .mapNotNull { node ->
        val mine = getGraphValue<String>(graph, node, MarlothProperties.mine)
        val other = getGraphValue<String>(graph, node, MarlothProperties.other)
        val cellDirection = getGraphValue<CellDirection>(graph, node, MarlothProperties.direction)
        val direction = cellDirection?.direction
        val height = getGraphValue<Int>(graph, node, MarlothProperties.sideHeight) ?: StandardHeights.first
        if (mine == null || other == null || direction == null)
          null
        else {
          direction to Side(
              mine = ConnectionContract(mine, height = height),
              other = ConnectionContract(other, height = height),
          )
        }
      }
      .associate { it }

  val isTraversible = sides.any { traversibleBlockSides.contains(it.value.mine.type) }
  val attributes = if (isTraversible)
    setOf(CellAttribute.isTraversable)
  else
    setOf()

  val showIfSideIsEmpty = filterByProperty(graph, MarlothProperties.showIfSideIsEmpty)

  val block = Block(
      name = name,
      sides = sides,
      attributes = attributes,
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
  val importedBlockBuilders = generationConfig.polyominoes
      .flatMap { (name, polyomino) ->
        blockBuildersFromElements(name, polyomino)
      }

  val blockBuilders = graphsToBlockBuilders(graphLibrary)
//  val blockBuilders = explodeBlockMap(allBlockBuilders() + importedBlockBuilders)
//  val blockBuilders = explodeBlockMap(allBlockBuilders())
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
//  val home = blocks.first { it.name == "home" }
  val home = blocks.first { it.name == "home-set" }
  val blockGrid = newBlockGrid(dice, home, blocks - home, generationConfig.roomCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, architectureSource)
}
