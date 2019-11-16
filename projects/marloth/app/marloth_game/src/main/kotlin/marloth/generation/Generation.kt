package marloth.generation

import generation.abstracted.initializeNodeRadii
import generation.architecture.definition.blockBuilders
import generation.architecture.definition.independentConnectionTypes
import generation.architecture.definition.rotatingConnectionTypes
import generation.elements.explodeBlockMap
import generation.generateRealm
import generation.misc.GenerationConfig
import generation.misc.newRandomizedBiomeGrid
import generation.next.buildArchitecture
import generation.next.newWorkbench
import marloth.definition.generation.biomeInfoMap
import mythic.ent.newIdSource
import org.recast4j.detour.NavMeshQuery
import randomly.Dice
import simulation.intellect.navigation.newNavMesh
import simulation.main.Deck
import simulation.main.World
import simulation.main.pipeHandsToDeck
import simulation.misc.WorldInput

fun generateWorld(generationConfig: GenerationConfig, input: WorldInput): World {
  val dice = input.dice
  val (blocks, builders) = blockBuilders()
  val independentConnections = independentConnectionTypes()
  val blockMap = explodeBlockMap(rotatingConnectionTypes(), blocks)
  val workbench = newWorkbench(dice, blockMap.keys, independentConnections, generationConfig.roomCount)
  val grid = workbench.mapGrid

  val biomeGrid = newRandomizedBiomeGrid(biomeInfoMap, input)
  val realm = generateRealm(generationConfig, input, grid, biomeGrid)
  val nextId = newIdSource(1)
  val deck = pipeHandsToDeck(nextId, listOf(
      { _ ->
        buildArchitecture(generationConfig, dice, independentConnections, workbench, blockMap, realm.cellBiomes, builders)
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
