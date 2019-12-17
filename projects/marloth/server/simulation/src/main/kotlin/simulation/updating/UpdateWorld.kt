package simulation.updating

import simulation.physics.updatePhysics
import simulation.combat.getDamageMultiplierModifiers
import simulation.combat.toModifierDeck
import simulation.entities.*
import simulation.happenings.*
import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.filterCharacterCommandsFromEvents
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.updateAiState
import simulation.main.*
import simulation.misc.Definitions
import simulation.particles.updateParticleEffect
import simulation.physics.*

const val simulationFps = 60
const val simulationDelta = 1f / simulationFps.toFloat()

fun generateIntermediateRecords(definitions: Definitions, previous: Deck, world: World, externalEvents: Events): Events {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck))
  val commands = filterCharacterCommandsFromEvents(externalEvents).plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
  val triggerEvents = (if (shouldUpdateLogic(world)) {
    val triggerings = gatherActivatedTriggers(deck, definitions, collisions, commands)
    triggersToEvents(triggerings)
        .plus(updateIntTimersEvent)
  } else
    listOf())

  val events = externalEvents
      .plus(triggerEvents)
      .plus(commandsToEvents(commands))
      .plus(commands)

  return events.plus(eventsFromEvents(previous, world, events))
}

val cleanupOutdatedReferences: (Deck) -> Deck = { deck ->
  deck.copy(
      attachments = deck.attachments
          .mapValues(mapEntryValue(cleanupAttachmentSource(deck)))
  )
}

fun updateEntities(animationDurations: AnimationDurationMap, world: World, events: Events): (Deck) -> Deck =
    { deck ->
      val delta = simulationDelta
      val dice = world.dice
      deck.copy(
          actions = updateActions(world.definitions, deck, events),
          ambientSounds = updateAmbientAudio(dice, deck),
          animations = mapTable(deck.animations, updateCharacterAnimation(deck, animationDurations, delta)),
          characterRigs = mapTable(deck.characterRigs, updateMarlothCharacterRig(world.bulletState, deck, events)),
          attachments = mapTable(deck.attachments, updateAttachment(events)),
          cycles = mapTableValues(deck.cycles, updateCycle(delta)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events)),
          characters = mapTable(deck.characters, updateCharacter(deck, world.bulletState, events)),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(dice, delta)),
          spirits = mapTable(deck.spirits, updateAiState(world, delta)),
          timers = updateIntTimers(events)(deck.timers),
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

fun updateDeck(animationDurations: AnimationDurationMap, definitions: Definitions, events: Events,
               world: World,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(animationDurations, world, events),
        ifUpdatingLogic(world, updateDeckCache(definitions)),
        removeWhole(events, world.deck),
        removePartial(events, world.deck),
        cleanupOutdatedReferences,
        newEntities(definitions, animationDurations, world.deck, events, nextId)
    )

fun updateWorldDeck(animationDurations: AnimationDurationMap, definitions: Definitions, events: Events,
                    delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val newDeck = updateDeck(animationDurations, definitions, events, world, nextId)(world.deck)
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(animationDurations: AnimationDurationMap, definitions: Definitions, previous: Deck,
                externalEvents: Events, delta: Float): (World) -> World =
    pipe2(listOf(
        { world ->
          val events = generateIntermediateRecords(definitions,previous, world, externalEvents)
          pipe2(listOf(
              updatePhysics(events),
              updateWorldDeck(animationDurations, definitions, events, delta)
          ))(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
