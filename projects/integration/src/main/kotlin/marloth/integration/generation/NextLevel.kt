package marloth.integration.generation

import marloth.integration.front.GameApp
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.newBulletStateWithGraph
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import simulation.main.*
import simulation.misc.Definitions
import simulation.misc.getPlayerStart

fun extractLongTermEntities(deck: Deck, playerStart: Matrix): List<NewHand> {
  return deck.players.keys.map { player ->
    val hand = copyEntity(deck, player)
    hand.copy(
        components = hand.components
            .filter { !(it is Body) }
            .plus(
                Body(
                    position = playerStart.translation(),
                    orientation = Quaternion().rotateZ(playerStart.rotation().z)
                )
            )
    )
  }
}

fun preserveLongTermEntities(definitions: Definitions, nextId: IdSource, graph: Graph, previous: Deck, next: Deck): Deck {
  val playerStart = getPlayerStart(graph)
  val hands = extractLongTermEntities(previous, playerStart!!)
  return allHandsToDeck(definitions, nextId, hands, next)
}

// nextLevel ignores runtime editor changes
fun nextLevel(app: GameApp, world: World): World {
  val global = world.global
  val level = global.level + 1
  val nextId = world.nextId.source()
  releaseBulletState(world.bulletState)
  val generationConfig = newGenerationConfig(app).copy(level = level)
  val (graph, deck) = generateWorldGraphAndDeck(nextId, generationConfig)
  return world.copy(
      deck = preserveLongTermEntities(world.definitions, nextId, graph, world.deck, deck),
      navigation = initializeNavigation(generationConfig, graph),
      staticGraph = graph,
      bulletState = newBulletStateWithGraph(graph, world.definitions.meshShapes),
      nextCommands = listOf(), // At minimum, the nextLevel command needs to be removed to prevent an infinite loop
      global = global.copy(
          level = level
      )
  )
}
