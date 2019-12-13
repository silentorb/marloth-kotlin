package simulation.updating

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.mapEntryValue
import silentorb.mythic.ent.pipe
import silentorb.mythic.ent.pipe2
import simulation.physics.updatePhysics
import silentorb.mythic.randomly.Dice
import simulation.combat.getDamageMultiplierModifiers
import simulation.combat.toModifierDeck
import simulation.entities.*
import simulation.happenings.*
import simulation.input.Commands
import simulation.input.updatePlayer
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.updateAiState
import simulation.main.*
import simulation.misc.Definitions
import simulation.particles.updateParticleEffect
import simulation.physics.*
import simulation.misc.Collisions

const val simulationFps = 60
const val simulationDelta = 1f / simulationFps.toFloat()

data class Intermediate(
    val commands: Commands,
    val collisions: Collisions,
    val events: Events
)

fun generateIntermediateRecords(definitions: Definitions, world: World, playerCommands: Commands,
                                externalEvents: Events): Intermediate {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck))
  val commands = playerCommands.plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
  val triggerEvents = (if (shouldUpdateLogic(world)) {
    val triggerings = gatherActivatedTriggers(deck, definitions, collisions, commands)
    triggersToEvents(triggerings)
  } else
    listOf())

  val events = externalEvents.plus(triggerEvents).plus(commandsToEvents(commands))

  return Intermediate(
      commands = commands,
      collisions = collisions,
      events = events.plus(eventsFromEvents(world, events))
  )
}

val cleanupOutdatedReferences: (Deck) -> Deck = { deck ->
  deck.copy(
      attachments = deck.attachments
          .mapValues(mapEntryValue(cleanupAttachmentSource(deck)))
  )
}

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World,
                   intermediate: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, collisionMap, events) = intermediate
      val delta = simulationDelta
      deck.copy(
          actions = updateActions(world.definitions, deck, events),
          ambientSounds = updateAmbientAudio(dice, deck),
          animations = mapTable(deck.animations, updateCharacterAnimation(deck, animationDurations, delta)),
          attachments = mapTable(deck.attachments, updateAttachment(intermediate.events)),
          cycles = mapTableValues(deck.cycles, updateCycle(delta)),
//          depictions = mapTable(deck.depictions, updateDepiction(deck, animationDurations)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events)),
          characters = mapTable(deck.characters, updateCharacter(deck, commands, events)),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(dice, delta)),
          players = mapTable(deck.players, updatePlayer(intermediate.commands)),
          spirits = mapTable(deck.spirits, updateAiState(world, delta)),
          timers = if (shouldUpdateLogic(world)) mapTableValues(deck.timers, updateTimer) else deck.timers,
          timersFloat = mapTableValues(deck.timersFloat, updateFloatTimer(delta))
      )
    }

fun updateDeckCache(definitions: Definitions): (Deck) -> Deck =
    { deck ->
      val damageModifierQuery = getDamageMultiplierModifiers(definitions, toModifierDeck(deck))
      deck.copy(
          destructibles = mapTable(deck.destructibles, updateDestructibleCache(damageModifierQuery))
      )
    }

fun updateDeck(animationDurations: AnimationDurationMap, definitions: Definitions, intermediate: Intermediate,
               world: World,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(world.dice, animationDurations, world, intermediate),
        ifUpdatingLogic(world, updateDeckCache(definitions)),
        removeWhole(intermediate.events),
        removePartial(intermediate.events),
        cleanupOutdatedReferences,
        newEntities(definitions, animationDurations, intermediate.events, nextId)
    )

fun updateWorldDeck(animationDurations: AnimationDurationMap, definitions: Definitions, intermediate: Intermediate,
                    delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val newDeck = updateDeck(animationDurations, definitions, intermediate, world, nextId)(world.deck)
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(animationDurations: AnimationDurationMap, playerCommands: Commands, definitions: Definitions,
                events: Events, delta: Float): (World) -> World =
    pipe2(listOf(
        { world ->
          val intermediate = generateIntermediateRecords(definitions, world, playerCommands, events)
          val linearForces = allCharacterMovements(world, intermediate.commands)
          pipe2(listOf(
              updatePhysics(linearForces),
              updateWorldDeck(animationDurations, definitions, intermediate, delta)
          ))(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
