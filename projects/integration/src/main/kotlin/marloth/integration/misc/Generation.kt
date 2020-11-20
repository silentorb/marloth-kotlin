package marloth.integration.misc

import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.compileArchitectureMeshInfo
import marloth.clienting.editing.loadDefaultWorldGraph
import marloth.clienting.rendering.loadBlocks
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.generation.population.populateWorld
import marloth.scenery.enums.MeshShapeMap
import persistence.Database
import persistence.queryEntries
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.commonPropertyDefinitions
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.ent.scenery.expandInstances
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import simulation.intellect.navigation.newNavigationState
import simulation.main.World
import simulation.main.idHandsToDeck
import simulation.main.newGlobalState
import simulation.misc.*

fun generateWorld(db: Database, definitions: Definitions, generationConfig: GenerationConfig, input: WorldInput, graph: Graph): World {
  val nextId = newIdSource(1)
  val dice = input.dice

  val deck = idHandsToDeck(populateWorld(nextId, generationConfig, graph))
  val navigation = if (generationConfig.includeEnemies) {
    val meshIds = deck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavigationState(meshIds, deck)
  } else
    null

  val persistence = queryEntries(db, persistenceTable).toSet()

  return World(
      graph = graph,
      deck = deck,
      realm = Realm(grid = MapGrid()),
      nextId = nextId(),
      dice = Dice(),
      global = newGlobalState(),
      availableIds = setOf(),
      navigation = navigation,
      bulletState = newBulletState(),
      definitions = definitions,
      gameModeConfig = newGameModeConfig(),
      persistence = persistence,
  )
}

fun newGenerationSeed(): Long =
    getDebugString("GENERATION_SEED")?.toLong() ?: System.currentTimeMillis()

fun generateWorld(db: Database, definitions: Definitions, meshInfo: MeshShapeMap, graph: Graph, seed: Long = newGenerationSeed()): World {
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
  return generateWorld(db, definitions, generationConfig, input, graph)
}
