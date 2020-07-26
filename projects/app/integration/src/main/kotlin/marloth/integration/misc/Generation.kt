package marloth.integration.misc

import generation.architecture.building.BlockBuilder
import generation.architecture.definition.*
import generation.architecture.misc.*
import generation.architecture.old.applyTurns
import generation.general.newRandomizedBiomeGrid
import generation.general.rotateSides
import marloth.generation.population.populateWorld
import marloth.scenery.enums.MeshShapeMap
import marloth.scenery.enums.meshAttributes
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.newGenericIdHand
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import simulation.intellect.navigation.newNavigationState
import simulation.main.*
import simulation.misc.*

fun fixedCellBiomes(grid: MapGrid): CellBiomeMap {
  val homeNode = grid.cells.entries.firstOrNull { it.value.attributes.contains(CellAttribute.home) }?.key
  val exitNode = grid.cells.entries.firstOrNull { it.value.attributes.contains(CellAttribute.exit) }?.key
  return listOfNotNull(
      if (homeNode != null) Pair(homeNode, BiomeId.home.name) else null,
      if (exitNode != null) Pair(exitNode, BiomeId.exit.name) else null
  )
      .associate { it }
}

fun explodeBlockMap(blockBuilders: Collection<BlockBuilder>): List<BlockBuilder> {
  assert(blockBuilders.all { it.block.name.isNotEmpty() })
  val needsRotatedVariations = blockBuilders
      .filter { (block, _) ->
        !block.attributes.contains(CellAttribute.lockedRotation) &&
            block.sides != rotateSides(1)(block.sides)
      }

  val rotated = needsRotatedVariations
      .flatMap { (block, builder) ->
        (1..3)
            .map { turns ->
              val rotation = Quaternion().rotateZ(applyTurns(turns))
              BlockBuilder(
                  block = block.copy(
                      sides = rotateSides(turns)(block.sides),
                      slots = block.slots.map { slot ->
                        rotation.transform(slot - floorOffset) + floorOffset
                      },
                      turns = turns
                  ),
                  builder = builder
              )
            }
      }

  return blockBuilders.toList() + rotated
}

fun generateWorld(definitions: Definitions, generationConfig: GenerationConfig, input: WorldInput): World {
  val dice = input.dice
  val blockBuilders = explodeBlockMap(allBlockBuilders())
//  blockBuilders.forEach { blockBuilder -> assert(blockBuilder.block.sides.all { it != null }) }
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
  val independentConnections = independentConnectionTypes()
  val openConnectionTypes = openConnectionTypes()
  val workbench = newWorkbench(dice, blocks, generationConfig.roomCount)
  val grid = workbench.mapGrid
  assert(grid.connections.any())
//  val gridSideMap = bakeSides(independentConnections, openConnectionTypes, grid.connections, workbench.blockGrid)
  val biomeGrid = newRandomizedBiomeGrid(biomeInfoMap, input)
  val cellBiomes = applyBiomesToGrid(grid, biomeGrid)
      .plus(fixedCellBiomes(grid))

  val realm = generateRealm(grid, cellBiomes)
  val nextId = newIdSource(1)
  val architectureInput = newArchitectureInput(generationConfig, dice, workbench, cellBiomes)
  val architectureSource = buildArchitecture(architectureInput, builders + Pair(homeBlock.block.name, homeBlock.builder!!))

  // The <Hand> specifier shouldn't be needed here but without it Kotlin is throwing an internal error referencing this line
  val architectureHands = architectureSource.map(newGenericIdHand<Hand>(nextId))
  val architectureDeck = idHandsToDeck(architectureHands)

  val navigation = if (generationConfig.includeEnemies) {
    val meshIds = architectureDeck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavigationState(meshIds, architectureDeck)
  } else
    null

  val lightHands = lightHandsFromDepictions(definitions.lightAttachments, architectureHands)

  val deck = pipeIdHandsToDeck(listOf(
      { _ -> lightHands },
      populateWorld(nextId, generationConfig, input, realm)
  ))(architectureDeck)

  return World(
      deck = deck,
      realm = realm,
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
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = getDebugString("NO_ENEMIES") != "1",
      roomCount = getDebugInt("BASE_ROOM_COUNT") ?: 30
  )
  val input = WorldInput(
      boundary,
      dice
  )
  return generateWorld(definitions, generationConfig, input)
}
