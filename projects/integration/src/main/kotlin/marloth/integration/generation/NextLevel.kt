package marloth.integration.generation

import marloth.integration.front.GameApp
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.newBulletStateWithGraph
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import simulation.main.*
import simulation.misc.Definitions
import simulation.misc.getPlayerStart

fun extractLongTermEntities(deck: Deck): Table<NewHand> =
    deck.players.keys.associateWith { player ->
      copyEntity(deck, player)
    }

fun addHandBody(hand: NewHand, transform: Matrix): NewHand =
    hand.copy(
        components = hand.components
            .filter { !(it is Body) }
            .plus(
                Body(
                    position = transform.translation(),
                    orientation = Quaternion().rotateZ(transform.rotation().z)
                )
            )
    )

// nextLevel ignores runtime editor changes
fun nextLevel(app: GameApp, world: World): World {
  val global = world.global
  val level = global.level + 1
  val nextId = world.nextId.source()
  releaseBulletState(world.bulletState)
  val persistentHands = extractLongTermEntities(world.deck)
  val generationConfig = newGenerationConfig(app).copy(level = level)
  val (graph, deck) = generateWorldGraphAndDeck(nextId, generationConfig)
  val playerStart = getPlayerStart(graph) ?: Matrix.identity
  val playerHands = persistentHands
      .filterKeys { world.deck.players.containsKey(it) }
      .map { hand ->
        addHandBody(hand.value, playerStart)
      }

  return world.copy(
      deck = allHandsToDeck(world.definitions, nextId, playerHands, deck),
      navigation = initializeNavigation(generationConfig, graph),
      staticGraph = graph,
      bulletState = newBulletStateWithGraph(graph, world.definitions.meshShapes),
      nextCommands = listOf(), // At minimum, the nextLevel command needs to be removed to prevent an infinite loop
      global = global.copy(
          level = level
      )
  )
}
