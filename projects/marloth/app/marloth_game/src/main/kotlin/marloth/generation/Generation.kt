package marloth.generation

import generation.architecture.definition.*
import generation.architecture.misc.*
import generation.general.explodeBlockMap
import generation.general.newRandomizedBiomeGrid
import generation.general.bakeSides
import marloth.generation.population.populateWorld
import marloth.integration.newGameModeConfig
import silentorb.mythic.debugging.getDebugSetting
import silentorb.mythic.ent.newIdSource
import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.ent.toIdHands
import silentorb.mythic.randomly.Dice
import simulation.intellect.navigation.newNavMesh
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary
import silentorb.mythic.physics.newBulletState
import simulation.main.pipeIdHandsToDeck

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
  val realm = generateRealm(generationConfig, input, grid, biomeGrid)
  val nextId = newIdSource(1)
  val deck = pipeIdHandsToDeck(listOf(
      { _ ->
        toIdHands(nextId, buildArchitecture(generationConfig, dice, gridSideMap, workbench, blockMap, realm.cellBiomes, builders))
      },
      populateWorld(nextId, generationConfig, input, realm)
  ))(Deck())

  val navMesh = if (generationConfig.includeEnemies)
    newNavMesh(grid, deck)
  else
    null

  return World(
      deck = deck,
      realm = realm,
      nextId = nextId(),
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0,
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
