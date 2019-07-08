package simulation.main

import simulation.evention.DamageEvent
import simulation.evention.gatherEvents
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.updateAiState
import mythic.ent.*
import simulation.physics.*
import randomly.Dice
import simulation.input.updatePlayer
import simulation.misc.*
import simulation.particles.updateParticleEffect

const val simulationDelta = 1f / 60f

typealias DeckSource = (IdSource) -> Deck?

data class Events(
    val damage: List<DamageEvent> = listOf(),
    val deckSource: DeckSource = { null }
)

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions,
    val events: Events
)

fun generateIntermediateRecords(bulletState: BulletState, world: World, playerCommands: Commands): Intermediate {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck).values)
  val commands = playerCommands.plus(spiritCommands)
  val collisions = getBulletCollisions(bulletState, deck)
  return Intermediate(
      commands = commands,
      activatedAbilities = getActivatedAbilities(deck, commands),
      collisions = collisions,
      events = if (shouldUpdateLogic(world)) gatherEvents(deck, collisions) else Events()
  )
}

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World, data: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap, events) = data

      val bodies = updateBodies(world.copy(deck = deck), commands, collisionMap)
      val bodyWorld = world.copy(
          deck = deck.copy(bodies = bodies)
      )

      deck.copy(
          ambientSounds = updateAmbientAudio(dice, deck),
          bodies = bodies,
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld, animationDurations)),
          characters = mapTable(deck.characters, updateCharacter(bodyWorld, commands, activatedAbilities, events.damage)),
          particleEffects = mapTableValues(deck.particleEffects, bodyWorld.deck.bodies, updateParticleEffect(dice, simulationDelta)),
          players = mapTable(deck.players, updatePlayer(data.commands)),
          spirits = mapTable(deck.spirits, updateAiState(bodyWorld, simulationDelta)),
          timers = if (shouldUpdateLogic(world)) updateTimers(deck.timers) else deck.timers
      )
    }

val removeEntities: (Deck) -> Deck = { deck ->
  val finished = getFinished(deck)
  removeEntities(deck, finished)
}

fun newEntities(events: Events, nextId: IdSource): (Deck) -> Deck = { deck ->
  val deckSource = events.deckSource
  val addition = deckSource(nextId)
  if (addition != null)
    deck.plus(addition)
  else
    deck
}

fun updateWorldDeck(animationDurations: AnimationDurationMap, intermediate: Intermediate, delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)

      val newDeck = pipe(world.deck, listOf(
          updateEntities(world.dice, animationDurations, world, intermediate),
          removeEntities,
          newEntities(intermediate.events, nextId)
      ))
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(bulletState: BulletState, animationDurations: AnimationDurationMap, playerCommands: Commands, delta: Float): (World) -> World =
    pipe(listOf(
        updateBulletPhysics(bulletState),
        { world ->
          val intermediate = generateIntermediateRecords(bulletState, world, playerCommands)
          updateWorldDeck(animationDurations, intermediate, delta)(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
