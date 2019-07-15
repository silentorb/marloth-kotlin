package simulation.main

import mythic.ent.IdSource
import mythic.ent.pipe
import randomly.Dice
import simulation.entities.*
import simulation.happenings.Events
import simulation.happenings.OrganizedEvents
import simulation.happenings.gatherEvents
import simulation.input.Commands
import simulation.input.updatePlayer
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.updateAiState
import simulation.misc.Definitions
import simulation.particles.updateParticleEffect
import simulation.physics.*

const val simulationFps = 60
const val simulationDelta = 1f / simulationFps.toFloat()

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions,
    val events: OrganizedEvents
)

fun generateIntermediateRecords(bulletState: BulletState, definitions: Definitions, world: World,
                                playerCommands: Commands, events: Events): Intermediate {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck).values)
  val commands = playerCommands.plus(spiritCommands)
  val collisions = getBulletCollisions(bulletState, deck)
  return Intermediate(
      commands = commands,
      activatedAbilities = getActivatedAbilities(deck, commands),
      collisions = collisions,
      events = if (shouldUpdateLogic(world)) gatherEvents(definitions, deck, collisions, events) else OrganizedEvents()
  )
}

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World, intermediate: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap, events) = intermediate

      val bodies = updateBodies(world.copy(deck = deck), commands, collisionMap)
      val bodyWorld = world.copy(
          deck = deck.copy(bodies = bodies)
      )

      deck.copy(
          ambientSounds = updateAmbientAudio(dice, deck),
          attachments = mapTable(deck.attachments, updateAttachment(intermediate.events)),
          bodies = bodies,
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld, animationDurations)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events.damage)),
          characters = mapTable(deck.characters, updateCharacter(bodyWorld.deck, commands, activatedAbilities, events.damage)),
          particleEffects = mapTableValues(deck.particleEffects, bodyWorld.deck.bodies, updateParticleEffect(dice, simulationDelta)),
          players = mapTable(deck.players, updatePlayer(intermediate.commands)),
          spirits = mapTable(deck.spirits, updateAiState(bodyWorld, simulationDelta)),
          timers = if (shouldUpdateLogic(world)) mapTableValues(deck.timers, updateTimer) else deck.timers
      )
    }

fun newEntities(events: OrganizedEvents, nextId: IdSource): (Deck) -> Deck = { deck ->
  mergeDecks(listOf(deck).plus(resolveDecks(nextId, events.decks)))
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

fun updateWorld(bulletState: BulletState, animationDurations: AnimationDurationMap, playerCommands: Commands,
                definitions: Definitions, events: Events, delta: Float): (World) -> World =
    pipe(listOf(
        updateBulletPhysics(bulletState),
        { world ->
          val intermediate = generateIntermediateRecords(bulletState, definitions, world, playerCommands, events)
          updateWorldDeck(animationDurations, intermediate, delta)(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
