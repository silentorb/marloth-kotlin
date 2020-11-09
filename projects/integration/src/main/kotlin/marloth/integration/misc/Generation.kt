package marloth.integration.misc

import generation.architecture.biomes.allBlockBuilders
import generation.architecture.matrical.BlockBuilder
import generation.architecture.engine.*
import generation.architecture.engine.applyTurns
import generation.general.BlockGrid
import generation.general.rotateSides
import marloth.clienting.rendering.loadBlocks
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshShapeMap
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.Graph
import silentorb.mythic.editing.commonPropertyDefinitions
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import simulation.intellect.navigation.newNavigationState
import simulation.main.*
import simulation.misc.*

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

fun generateWorldBlocks(dice: Dice, generationConfig: GenerationConfig): Pair<BlockGrid, List<Hand>> {
  val importedBlockBuilders = generationConfig.polyominoes
      .flatMap { (name, polyomino) ->
        blockBuildersFromElements(name, polyomino)
      }

  val blockBuilders = explodeBlockMap(allBlockBuilders() + importedBlockBuilders)
//  val blockBuilders = explodeBlockMap(allBlockBuilders())
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
//  val home = blocks.first { it.name == "home" }
  val home = blocks.first { it.name == "home-1" }
  val blockGrid = newBlockGrid(dice, home, blocks - home, generationConfig.roomCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, architectureSource)
}

fun worldFromGraph(generationConfig: GenerationConfig, dice: Dice, graph: Graph): Deck {
  return Deck()
}

fun generateWorld(definitions: Definitions, generationConfig: GenerationConfig, input: WorldInput): World {
  val nextId = newIdSource(1)
  val dice = input.dice
//  val getSpatialNode = compileWorldGenerationCode()
//  val root = getSpatialNode(SpatialNodeInput(meshes = generationConfig.meshes))

//  val (blockGrid, architectureSource) = generateWorldBlocks(dice, generationConfig)
//  val grid = mapGridFromBlockGrid(blockGrid)
//  assert(grid.connections.any())

  // The <Hand> specifier shouldn't be needed here but without it Kotlin is throwing an internal error referencing this line
//  val architectureHands = architectureSource.map(newGenericIdHand<Hand>(nextId))
//  val architectureDeck = idHandsToDeck(architectureHands)

//
//  val lightHands = lightHandsFromDepictions(definitions.lightAttachments, architectureHands)
//
//  val realm = generateRealm(grid)
//  val deck = pipeIdHandsToDeck(listOf(
//      { _ -> lightHands },
//      populateWorld(nextId, generationConfig, input, realm)
//  ))(architectureDeck)

  val graphLibrary = loadMarlothGraphLibrary(commonPropertyDefinitions())
  val rootGraph = graphLibrary["root"]!!
  val deck = worldFromGraph(generationConfig, dice, rootGraph)
  val navigation = if (generationConfig.includeEnemies) {
    val meshIds = deck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavigationState(meshIds, deck)
  } else
    null

  return World(
      deck = deck,
      realm = Realm(grid = MapGrid()),
      nextId = nextId(),
      dice = Dice(),
      global = newGlobalState(),
      availableIds = setOf(),
      navigation = navigation,
      bulletState = newBulletState(),
      definitions = definitions,
      gameModeConfig = newGameModeConfig()
  )
}

fun newGenerationSeed(): Long =
    getDebugString("GENERATION_SEED")?.toLong() ?: System.currentTimeMillis()

fun generateWorld(definitions: Definitions, meshInfo: MeshShapeMap, seed: Long = newGenerationSeed()): World {
  val dice = Dice(seed)
  if (getDebugBoolean("LOG_SEED")) {
    println("Generation seed: ${dice.seed}")
  }
  val boundary = createWorldBoundary(100f)
  val generationConfig = GenerationConfig(
      definitions = definitions,
      meshes = compileArchitectureMeshInfo(meshInfo),
      includeEnemies = getDebugString("NO_ENEMIES") != "1",
      roomCount = getDebugInt("BASE_ROOM_COUNT") ?: 100,
      polyominoes = loadBlocks()
  )
  val input = WorldInput(
      boundary,
      dice
  )
  return generateWorld(definitions, generationConfig, input)
}
