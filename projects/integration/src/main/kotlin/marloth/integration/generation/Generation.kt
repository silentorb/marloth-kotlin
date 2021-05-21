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
import silentorb.mythic.ent.scenery.removeNodesAndChildren
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.physics.newBulletStateWithGraph
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.newNavigationState
import simulation.main.Deck
import simulation.main.World
import simulation.main.allHandsToDeck
import simulation.main.newGlobalState
import simulation.misc.Definitions
import simulation.misc.MarlothProperties

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
                      graph: Graph, deck: Deck): Deck {
  val hands = populateWorld(nextId, generationConfig, dice, graph)
  return allHandsToDeck(definitions, nextId, hands, deck)
}

fun initializeNavigation(generationConfig: GenerationConfig, graph: Graph): NavigationState? =
    if (generationConfig.includeEnemies) {
      val meshNodes = filterByProperty(graph, SceneProperties.collisionShape)
          .map { it.source }
      newNavigationState(generationConfig.definitions.meshShapes, meshNodes, graph, setOf(), Deck())
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
        bulletState = newBulletStateWithGraph(graph, generationConfig.meshShapes),
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

fun generateWorldGraphAndDeck(nextId: IdSource, generationConfig: GenerationConfig,
                              dice: Dice = Dice(generationConfig.seed)): Pair<Graph, Deck> {
  val definitions = generationConfig.definitions
  val seed = generationConfig.seed
  println("Generation seed $seed")
  if (getDebugBoolean("LOG_SEED")) {
    println("Generation seed: ${dice.seed}")
  }

  val graph = prepareWorldGraph(generationConfig, dice, listOf())
  val staticEntities = graph
      .filter { it.property == MarlothProperties.interaction }
      .map { it.source }
      .toSet()

  val staticHands = graphToHands(definitions.meshShapes, nextId, graph, staticEntities, Matrix.identity)
  val initialDeck = allHandsToDeck(definitions, nextId, staticHands, Deck())
  val graph2 = removeNodesAndChildren(graph, staticEntities)
  val deck = generateWorldDeck(nextId, definitions, generationConfig, dice, graph2, initialDeck)
  return graph2 to deck
}

fun generateNewWorld(db: Database, graph: Graph, generationConfig: GenerationConfig): World {
  val nextId = newIdSource(1)
  val (graph2, deck) = generateWorldGraphAndDeck(nextId, generationConfig)
  val deck2 = addNewPlayerCharacters(nextId, generationConfig, graph2, deck)
  return newWorld(db, generationConfig, nextId, graph2, deck2)
}
