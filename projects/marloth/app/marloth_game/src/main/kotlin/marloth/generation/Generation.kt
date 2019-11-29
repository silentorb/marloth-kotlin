package marloth.generation

import generation.abstracted.initializeNodeRadii
import generation.architecture.definition.*
import generation.architecture.misc.*
import generation.general.explodeBlockMap
import generation.general.newRandomizedBiomeGrid
import generation.general.bakeSides
import mythic.ent.getDebugSetting
import mythic.ent.newIdSource
import org.recast4j.detour.NavMeshQuery
import randomly.Dice
import simulation.intellect.navigation.newNavMesh
import simulation.main.Deck
import simulation.main.World
import simulation.main.pipeHandsToDeck
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary

fun generateWorld(generationConfig: GenerationConfig, input: WorldInput): World {
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
  val deck = pipeHandsToDeck(nextId, listOf(
      { _ ->
        buildArchitecture(generationConfig, dice, gridSideMap, workbench, blockMap, realm.cellBiomes, builders)
      },
      populateWorld(generationConfig, input, realm)
  ))(Deck())

  val navMesh = if (generationConfig.includeEnemies)
    newNavMesh(deck)
  else
    null

  return World(
      deck = deck,
      realm = realm.copy(
          graph = initializeNodeRadii(deck)(realm.graph)
      ),
      nextId = nextId(),
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0,
      navMesh = navMesh,
      navMeshQuery = if (navMesh != null) NavMeshQuery(navMesh) else null
  )
}

fun newGenerationDice() =
    Dice(getDebugSetting("GENERATION_SEED")?.toLong())

fun generateWorld(meshInfo: MeshShapeMap, dice: Dice = newGenerationDice()): World {
  val boundary = createWorldBoundary(100f)
  val generationConfig = GenerationConfig(
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = true,
      roomCount = 20
  )
  val input = WorldInput(
      boundary,
      dice
  )
  return generateWorld(generationConfig, input)
}
