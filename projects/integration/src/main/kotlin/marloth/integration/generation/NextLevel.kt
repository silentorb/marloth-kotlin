package marloth.integration.generation

import marloth.integration.front.GameApp
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.newBulletStateWithGraph
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import simulation.characters.upgradeCharacterEquipment
import simulation.main.*
import simulation.misc.Definitions
import simulation.misc.getPlayerStart

fun extractLongTermEntities(deck: Deck): Table<NewHand> =
    deck.characters.keys.associateWith { character ->
      copyEntity(deck, character)
    }

// nextLevel ignores runtime editor changes
fun nextLevel(app: GameApp, world: World): World {
  val global = world.global
  val definitions = world.definitions
  val level = global.level + 1
  val nextId = world.nextId.source()
  releaseBulletState(world.bulletState)
  val players = world.deck.players
  val initialConfig = newGenerationConfig(app)
  val dice = Dice(initialConfig.seed)
  val persistentHands = extractLongTermEntities(world.deck)
      .mapValues { (id, hand) ->
        val character = world.deck.characters[id]
        if (!players.containsKey(id) && character != null)
          upgradeCharacterEquipment(definitions, dice, character.definition, hand)
        else
          hand
      }

  val generationConfig = initialConfig
      .copy(
          hands = persistentHands,
          level = level,
      )

  val (graph, deck) = generateWorldGraphAndDeck(nextId, generationConfig, dice)
  val playerStart = getPlayerStart(graph) ?: Matrix.identity
  val playerHands = persistentHands
      .filterKeys { players.containsKey(it) }
      .map { hand ->
        addHandBody(hand.value, playerStart)
      }

  return world.copy(
      deck = allHandsToDeck(definitions, nextId, playerHands, deck),
      navigation = initializeNavigation(generationConfig, graph),
      staticGraph = graph,
      bulletState = newBulletStateWithGraph(graph, definitions.meshShapes),
      nextCommands = listOf(), // At minimum, the nextLevel command needs to be removed to prevent an infinite loop
      global = global.copy(
          level = level
      )
  )
}
