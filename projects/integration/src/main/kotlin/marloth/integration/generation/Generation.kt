package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.compileArchitectureMeshInfo
import generation.general.BlockGrid
import marloth.clienting.editing.marlothPropertiesSerialization
import marloth.clienting.editing.staticDebugBlockGrid
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.generation.population.populateWorld
import marloth.integration.misc.newGameModeConfig
import marloth.integration.misc.persistenceTable
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
import simulation.main.Deck
import simulation.main.World
import simulation.main.allHandsToDeck
import simulation.main.newGlobalState
import simulation.misc.Definitions
import simulation.misc.Realm

fun generateWorld(db: Database, definitions: Definitions, generationConfig: GenerationConfig, dice: Dice, graph: Graph, step: Long): World {
  val nextId = newIdSource(1)
  val graph2: Graph = if (!getDebugBoolean("STATIC_MAP")) {
    val (blockGrid, architectureSource) = generateWorldBlocks(dice, generationConfig, generationConfig.graphLibrary)
    if (getDebugBoolean("ENABLE_EDITOR")) {
      staticDebugBlockGrid = blockGrid
    }
    graph + architectureSource
  } else
    graph

  val realm = Realm(Deck())

  val deck = allHandsToDeck(definitions, nextId, populateWorld(nextId, generationConfig, dice, graph2), step, Deck())
  val navigation = if (generationConfig.includeEnemies) {
    val meshNodes = filterByProperty(graph2, SceneProperties.collisionShape)
        .map { it.source }
    newNavigationState(definitions.meshShapeMap, meshNodes, graph2, setOf(), Deck())
  } else
    null

  val persistence = queryEntries(db, persistenceTable).toSet()

  return World(
      staticGraph = graph2,
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

fun generateWorld(db: Database, definitions: Definitions, meshInfo: MeshShapeMap, graph: Graph,
                  graphLibrary: GraphLibrary,
                  seed: Long = newGenerationSeed()): World {
  println("Generation seed $seed")
  val dice = Dice(seed)
  if (getDebugBoolean("LOG_SEED")) {
    println("Generation seed: ${dice.seed}")
  }
  val generationConfig = GenerationConfig(
      seed = seed,
      definitions = definitions,
      meshes = compileArchitectureMeshInfo(meshInfo),
      includeEnemies = getDebugString("MONSTER_LIMIT") != "0",
      meshShapes = meshInfo,
      cellCount = getDebugInt("BASE_ROOM_COUNT") ?: 100,
      polyominoes = mapOf(),
      graphLibrary = loadMarlothGraphLibrary(marlothPropertiesSerialization) + graphLibrary,
  )

  val graph2 = if (!getDebugBoolean("STATIC_MAP"))
    setOf()
  else
    graph

  return generateWorld(db, definitions, generationConfig, dice, graph2, 0L)
}
