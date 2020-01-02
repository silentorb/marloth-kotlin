package marloth.integration

import generation.architecture.definition.*
import generation.architecture.misc.*
import generation.general.bakeSides
import generation.general.explodeBlockMap
import generation.general.newRandomizedBiomeGrid
import marloth.generation.population.populateWorld
import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.debugging.getDebugSetting
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.ent.toIdHands
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import simulation.intellect.navigation.newNavMesh
import simulation.main.Deck
import simulation.main.World
import simulation.main.pipeIdHandsToDeck
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
  val (blocks, builders) = blockBuilders()
  val independentConnections = independentConnectionTypes()
  val openConnectionTypes = openConnectionTypes()
  val blockMap = explodeBlockMap(rotatingConnectionTypes(), blocks)
  val workbench = newWorkbench(dice, blockMap.keys, independentConnections, openConnectionTypes, generationConfig.roomCount)
  val grid = workbench.mapGrid
  val gridSideMap = bakeSides(independentConnections, openConnectionTypes, grid.connections, workbench.blockGrid)
  val biomeGrid = newRandomizedBiomeGrid(biomeInfoMap, input)
  val cellBiomes = applyBiomesToGrid(grid, biomeGrid)
      .plus(fixedCellBiomes(grid))

  val realm = generateRealm(grid, cellBiomes)
  val nextId = newIdSource(1)
  val architectureSource = buildArchitecture(generationConfig, dice, gridSideMap, workbench, blockMap, realm.cellBiomes, builders)
  val architectureHands = architectureSource.mapValues { (_, value) ->
    val hands = toIdHands(nextId, value)
    hands.plus(lightHandsFromDepictions(definitions.lightAttachments, hands))
  }
  val architectureCells = mapArchitectureCells(architectureHands)
  val deck = pipeIdHandsToDeck(listOf(
      { _ ->
        architectureHands.flatMap { it.value }
      },
      populateWorld(nextId, generationConfig, input, realm, architectureCells)
  ))(Deck())

  val navMesh = if (generationConfig.includeEnemies) {
    val meshIds = deck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavMesh(meshIds, deck)
  } else
    null

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
    Dice(getDebugSetting("GENERATION_SEED")?.toLong())

fun generateWorld(definitions: Definitions, meshInfo: MeshShapeMap, dice: Dice = newGenerationDice()): World {
  val boundary = createWorldBoundary(100f)
  val generationConfig = GenerationConfig(
      definitions = definitions,
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = getDebugSetting("NO_ENEMIES") != "1",
      roomCount = 20
  )
  val input = WorldInput(
      boundary,
      dice
  )
  return generateWorld(definitions, generationConfig, input)
}
