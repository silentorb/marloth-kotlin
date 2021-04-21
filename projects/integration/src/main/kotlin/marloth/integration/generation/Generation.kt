package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import generation.architecture.engine.compileArchitectureMeshInfo
import marloth.clienting.editing.marlothPropertiesSerialization
import marloth.clienting.editing.staticDebugBlockGrid
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.integration.front.GameApp
import marloth.integration.misc.persistenceTable
import marloth.scenery.enums.MeshShapeMap
import persistence.Database
import persistence.queryEntries
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.*
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.physics.newBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.newNavigationState
import simulation.main.Deck
import simulation.main.World
import simulation.main.allHandsToDeck
import simulation.main.newGlobalState
import simulation.misc.Definitions

fun prepareWorldGraph(generationConfig: GenerationConfig, dice: Dice, graph: Graph) =
    if (getDebugBoolean("STATIC_MAP"))
      graph
    else {
      val (blockGrid, architectureSource) = generateWorldBlocks(dice, generationConfig, generationConfig.graphLibrary)
      if (getDebugBoolean("ENABLE_EDITOR")) {
        staticDebugBlockGrid = blockGrid
      }
      architectureSource
    }

fun generateWorldDeck(nextId: IdSource, definitions: Definitions, generationConfig: GenerationConfig, dice: Dice,
                      graph: Graph): Deck {
  val hands = populateWorld(nextId, generationConfig, dice, graph)
  return allHandsToDeck(definitions, nextId, hands, Deck())
}

fun initializeNavigation(generationConfig: GenerationConfig, graph: Graph): NavigationState? =
    if (generationConfig.includeEnemies) {
      val meshNodes = filterByProperty(graph, SceneProperties.collisionShape)
          .map { it.source }
      newNavigationState(generationConfig.definitions.meshShapeMap, meshNodes, graph, setOf(), Deck())
    } else
      null

fun newWorld(db: Database, generationConfig: GenerationConfig, nextId: IdSource, graph: Graph, deck: Deck): World =
    World(
        nextId = SharedNextId(nextId()),
        deck = deck,
        global = newGlobalState(),
        dice = Dice(),
        navigation = initializeNavigation(generationConfig, graph),
        staticGraph = graph,
        bulletState = newBulletState(),
        definitions = generationConfig.definitions,
        persistence = queryEntries(db, persistenceTable).toSet(),
        step = 0L,
    )

fun newGenerationSeed(): Long =
    getDebugString("GENERATION_SEED")?.toLong() ?: System.currentTimeMillis()

fun newGenerationConfig(definitions: Definitions, meshInfo: MeshShapeMap,
                        graphLibrary: GraphLibrary,
                        seed: Long = newGenerationSeed()): GenerationConfig =
    GenerationConfig(
        seed = seed,
        definitions = definitions,
        meshes = compileArchitectureMeshInfo(meshInfo),
        meshShapes = meshInfo,
        includeEnemies = getDebugString("MONSTER_LIMIT") != "0",
        cellCount = getDebugInt("BASE_ROOM_COUNT") ?: 100,
        graphLibrary = loadMarlothGraphLibrary(marlothPropertiesSerialization) + graphLibrary,
    )

fun newGenerationConfig(gameApp: GameApp,
                        graphLibrary: GraphLibrary = mapOf(),
                        seed: Long = newGenerationSeed()): GenerationConfig =
    newGenerationConfig(gameApp.definitions, getMeshShapes(gameApp.client.renderer), graphLibrary, seed)

fun generateWorldGraphAndDeck(nextId: IdSource, generationConfig: GenerationConfig, graph: Graph = listOf()): Pair<Graph, Deck> {
  val definitions = generationConfig.definitions
  val seed = generationConfig.seed
  println("Generation seed $seed")
  val dice = Dice(seed)
  if (getDebugBoolean("LOG_SEED")) {
    println("Generation seed: ${dice.seed}")
  }

  val graph2 = prepareWorldGraph(generationConfig, dice, graph)
  val deck = generateWorldDeck(nextId, definitions, generationConfig, dice, graph2)
  return graph2 to deck
}

fun generateNewWorld(db: Database, graph: Graph, generationConfig: GenerationConfig): World {
  val nextId = newIdSource(1)
  val (graph2, deck) = generateWorldGraphAndDeck(nextId, generationConfig, graph)
  val deck2 = addNewPlayerCharacters(nextId, generationConfig, graph2, deck)
  return newWorld(db, generationConfig, nextId, graph2, deck2)
}
