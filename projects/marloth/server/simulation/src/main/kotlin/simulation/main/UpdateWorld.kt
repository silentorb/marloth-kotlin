package simulation.main

import mythic.ent.IdSource
import mythic.ent.mapEntryValue
import mythic.ent.pipe
import mythic.ent.pipe2
import randomly.Dice
import simulation.combat.getDamageMultiplierModifiers
import simulation.combat.toModifierDeck
import simulation.entities.*
import simulation.happenings.*
import simulation.input.Commands
import simulation.input.updatePlayer
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.updateAiState
import simulation.misc.Definitions
import simulation.particles.updateParticleEffect
import simulation.physics.*
import simulation.physics.old.Collisions

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
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck))
  val commands = playerCommands.plus(spiritCommands)
  val collisions = getBulletCollisions(bulletState, deck)
  val triggers = (if (shouldUpdateLogic(world))
    gatherActivatedTriggers(deck, definitions, collisions)
  else
    listOf())
      .plus(gatherCommandTriggers(deck, commands))

  return Intermediate(
      commands = commands,
      activatedAbilities = getActivatedAbilities(deck, commands),
      collisions = collisions,
      events = gatherEvents(definitions, deck, triggers, events)
  )
}

val cleanupOutdatedReferences: (Deck) -> Deck = { deck ->
  deck.copy(
      attachments = deck.attachments
          .mapValues(mapEntryValue(cleanupAttachmentSource(deck)))
  )
}

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World,
                   worldQuerySource: WorldQuerySource, intermediate: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap, events) = intermediate
      deck.copy(
          ambientSounds = updateAmbientAudio(dice, deck),
          attachments = mapTable(deck.attachments, updateAttachment(intermediate.events)),
          bodies = mapTableValues(deck.bodies, updateBody(world.realm)),
          cycles = mapTableValues(deck.cycles, updateCycle(simulationDelta)),
          depictions = mapTable(deck.depictions, updateDepiction(deck, animationDurations)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events.damage)),
          characters = mapTable(deck.characters, updateCharacter(deck, commands, activatedAbilities, events)),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(dice, simulationDelta)),
          players = mapTable(deck.players, updatePlayer(intermediate.commands)),
          spirits = mapTable(deck.spirits, updateAiState(world, worldQuerySource, simulationDelta)),
          timers = if (shouldUpdateLogic(world)) mapTableValues(deck.timers, updateTimer) else deck.timers
      )
    }

fun updateDeckCache(definitions: Definitions): (Deck) -> Deck =
    { deck ->
      val damageModifierQuery = getDamageMultiplierModifiers(definitions, toModifierDeck(deck))
      deck.copy(
          destructibles = mapTable(deck.destructibles, updateDestructibleCache(damageModifierQuery))
      )
    }

fun newEntities(events: OrganizedEvents, nextId: IdSource): (Deck) -> Deck = { deck ->
  val additional = Deck(
      accessories = newAccessories(events, deck)
  )
  mergeDecks(listOf(deck, additional).plus(resolveDecks(nextId, events.decks)))
}

fun updateDeck(animationDurations: AnimationDurationMap, definitions: Definitions, intermediate: Intermediate,
               world: World,
               worldQuerySource: WorldQuerySource,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(world.dice, animationDurations, world, worldQuerySource, intermediate),
        ifUpdatingLogic(world, updateDeckCache(definitions)),
        removeWhole(intermediate.events),
        removePartial(intermediate.events),
        cleanupOutdatedReferences,
        newEntities(intermediate.events, nextId)
    )

fun updateWorldDeck(animationDurations: AnimationDurationMap, definitions: Definitions, intermediate: Intermediate,
                    worldQuerySource: WorldQuerySource,
                    delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val newDeck = updateDeck(animationDurations, definitions, intermediate, world, worldQuerySource, nextId)(world.deck)
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(bulletState: BulletState, animationDurations: AnimationDurationMap, playerCommands: Commands,
                definitions: Definitions, events: Events, delta: Float): (World) -> World =
    pipe2(listOf(
        { world ->
          val intermediate = generateIntermediateRecords(bulletState, definitions, world, playerCommands, events)
          val linearForces = allCharacterMovements(world, intermediate.commands)
          pipe2(listOf(
              updateBulletPhysics(bulletState, linearForces),
              updateWorldDeck(animationDurations, definitions, intermediate, BulletQuerySource(bulletState), delta)
          ))(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
