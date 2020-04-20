package marloth.integration.misc

import generation.architecture.definition.*
import generation.architecture.misc.*
import generation.general.bakeSides
import generation.general.explodeBlockMap
import generation.general.newRandomizedBiomeGrid
import marloth.generation.population.populateWorld
import marloth.scenery.enums.MeshShapeMap
import marloth.scenery.enums.meshAttributes
import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.newGenericIdHand
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import simulation.intellect.navigation.newNavMesh
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

fun generateWorld(definitions: Definitions, generationConfig: GenerationConfig, input: WorldInput): World {
  val dice = input.dice
  val blockBuilders = allBlockBuilders()
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders.values))
  val independentConnections = independentConnectionTypes()
  val openConnectionTypes = openConnectionTypes()
  val blockMap = explodeBlockMap(rotatingConnectionTypes(), blocks)
  val blockOptions = blockMap.keys//.minus(blockBuilders["home"]!!.block)
  val workbench = newWorkbench(dice, blockOptions, independentConnections, openConnectionTypes, generationConfig.roomCount)
  val grid = workbench.mapGrid
  val gridSideMap = bakeSides(independentConnections, openConnectionTypes, grid.connections, workbench.blockGrid)
  val biomeGrid = newRandomizedBiomeGrid(biomeInfoMap, input)
  val cellBiomes = applyBiomesToGrid(grid, biomeGrid)
      .plus(fixedCellBiomes(grid))

  val realm = generateRealm(grid, cellBiomes)
  val nextId = newIdSource(1)
  val architectureInput = newArchitectureInput(generationConfig, dice, gridSideMap, workbench, cellBiomes)
  val architectureSource = buildArchitecture(architectureInput, blockMap, builders)

  // The <Hand> specifier shouldn't be needed here but without it Kotlin is throwing an internal error referencing this line
  val architectureHands = architectureSource.map(newGenericIdHand<Hand>(nextId))
  val architectureDeck = idHandsToDeck(architectureHands)

  val navMesh = if (generationConfig.includeEnemies) {
    val meshIds = architectureDeck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavMesh(meshIds, architectureDeck)
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
      availableIds = setOf(),
      navMesh = navMesh,
      navMeshQuery = if (navMesh != null) NavMeshQuery(navMesh) else null,
      bulletState = newBulletState(),
      definitions = definitions,
      gameModeConfig = newGameModeConfig()
  )
}

fun newGenerationDice() =
    Dice(getDebugString("GENERATION_SEED")?.toLong())

fun generateWorld(definitions: Definitions, meshInfo: MeshShapeMap, dice: Dice = newGenerationDice()): World {
  val boundary = createWorldBoundary(100f)
  val generationConfig = GenerationConfig(
      definitions = definitions,
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = getDebugString("NO_ENEMIES") != "1",
      roomCount = getDebugInt("BASE_ROOM_COUNT") ?: 10
  )
  val input = WorldInput(
      boundary,
      dice
  )
  return generateWorld(definitions, generationConfig, input)
}
