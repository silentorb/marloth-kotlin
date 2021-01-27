package marloth.integration.misc

import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.compileArchitectureMeshInfo
import generation.general.mapGridFromBlockGrid
import marloth.clienting.rendering.loadBlocks
import marloth.generation.population.populateWorld
import marloth.scenery.enums.MeshShapeMap
import persistence.Database
import persistence.queryEntries
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.*
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import simulation.intellect.navigation.newNavigationState
import simulation.main.*
import simulation.misc.Definitions
import simulation.misc.MapGrid
import simulation.misc.Realm
import simulation.misc.lightHandsFromDepictions

fun generateWorld(db: Database, definitions: Definitions, generationConfig: GenerationConfig, dice: Dice, graph: Graph, step: Long): World {
  val nextId = newIdSource(1)
  val (grid, architectureSource) = if (getDebugBoolean("USE_MAP_GRID")) {
    val (blockGrid, architectureSource) = generateWorldBlocks(dice, generationConfig)
    mapGridFromBlockGrid(blockGrid) to architectureSource
  } else
    MapGrid() to listOf()

  // The <Hand> specifier shouldn't be needed here but without it Kotlin is throwing an internal error referencing this line
  val architectureHands = architectureSource.map(newGenericIdHand<Hand>(nextId))
  val architectureDeck = idHandsToDeck(architectureHands)

  val realm = Realm(grid, architectureDeck)

  val lightHands = if (getDebugBoolean("USE_MAP_GRID"))
    lightHandsFromDepictions(definitions.lightAttachments, architectureHands)
  else
    listOf()

  val deck = allHandsToDeck(nextId, populateWorld(nextId, generationConfig, dice, graph, grid), step, idHandsToDeck(lightHands))
  val navigation = if (generationConfig.includeEnemies) {
    val meshNodes = filterByProperty2(graph, SceneProperties.collisionShape)
        .map { it.source }
    val meshEntities = architectureDeck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavigationState(definitions.meshShapeMap, meshNodes, graph, meshEntities, architectureDeck)
  } else
    null

  val persistence = queryEntries(db, persistenceTable).toSet()

  return World(
      staticGraph = graph,
      deck = deck,
      realm = realm,
      nextId = nextId(),
      dice = Dice(),
      global = newGlobalState(),
      availableIds = setOf(),
      navigation = navigation,
      bulletState = newBulletState(),
      definitions = definitions,
      gameModeConfig = newGameModeConfig(),
      persistence = persistence,
      graph = SimpleGraphStore(),
      step = 0L,
  )
}

fun newGenerationSeed(): Long =
    getDebugString("GENERATION_SEED")?.toLong() ?: System.currentTimeMillis()

fun generateWorld(db: Database, definitions: Definitions, meshInfo: MeshShapeMap, graph: Graph, seed: Long = newGenerationSeed()): World {
  val dice = Dice(seed)
  if (getDebugBoolean("LOG_SEED")) {
    println("Generation seed: ${dice.seed}")
  }
  val generationConfig = GenerationConfig(
      definitions = definitions,
      meshes = compileArchitectureMeshInfo(meshInfo),
      includeEnemies = getDebugString("MONSTER_LIMIT") != "0",
      meshShapes = meshInfo,
      roomCount = getDebugInt("BASE_ROOM_COUNT") ?: 100,
      polyominoes = mapOf(),
  )

  val graph2 = if (getDebugBoolean("USE_MAP_GRID"))
    setOf()
  else
    graph

  return generateWorld(db, definitions, generationConfig, dice, graph2, 0L)
}
