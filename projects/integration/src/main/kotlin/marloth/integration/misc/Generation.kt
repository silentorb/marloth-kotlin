package marloth.integration.misc

import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.compileArchitectureMeshInfo
import marloth.generation.population.populateWorld
import marloth.scenery.enums.MeshShapeMap
import persistence.Database
import persistence.queryEntries
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.SimpleGraphStore
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import simulation.intellect.navigation.newNavigationState
import simulation.main.*
import simulation.misc.Definitions
import simulation.misc.GameAttributes
import simulation.misc.MapGrid
import simulation.misc.Realm
import simulation.physics.graphToBody

fun generateWorld(db: Database, definitions: Definitions, generationConfig: GenerationConfig, dice: Dice, graph: Graph): World {
  val nextId = newIdSource(1)
  val deck = allHandsToDeck(nextId, populateWorld(nextId, generationConfig, graph), Deck())
//  val graphHands = graphToHands(nextId, graph)
  val navigation = if (generationConfig.includeEnemies) {
    val meshIds = deck.depictions
        .filterValues { generationConfig.meshes.containsKey(it.mesh) }
        .keys
    newNavigationState(meshIds, deck)
  } else
    null

  val persistence = queryEntries(db, persistenceTable).toSet()

  return World(
      staticGraph = graph,
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
      graph = SimpleGraphStore(),
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
      includeEnemies = getDebugString("NO_ENEMIES") != "1"
  )

  return generateWorld(db, definitions, generationConfig, dice, graph)
}
