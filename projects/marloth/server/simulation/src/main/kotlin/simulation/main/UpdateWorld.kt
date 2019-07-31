package simulation.main

import mythic.ent.IdSource
import mythic.ent.pipe
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
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck).values)
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
      attachments = deck.attachments.mapValues {
        val source = if (it.value.source > 0 && deck.bodies.containsKey(it.value.source))
          it.value.source
        else
          0L
        it.value.copy(
            source = source
        )
      }
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
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld.deck, animationDurations)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events.damage)),
          characters = mapTable(deck.characters, updateCharacter(bodyWorld.deck, commands, activatedAbilities, events)),
          particleEffects = mapTableValues(deck.particleEffects, bodyWorld.deck.bodies, updateParticleEffect(dice, simulationDelta)),
          players = mapTable(deck.players, updatePlayer(intermediate.commands)),
          spirits = mapTable(deck.spirits, updateAiState(bodyWorld, simulationDelta)),
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

fun updateWorldDeck(animationDurations: AnimationDurationMap, definitions: Definitions, intermediate: Intermediate,
                    delta: Float): (World) -> World =
    { world ->
      val events = intermediate.events
      val (nextId, finalize) = newIdSource(world)

      val newDeck = pipe(world.deck, listOf(
          updateEntities(world.dice, animationDurations, world, intermediate),
          ifUpdatingLogic(world, updateDeckCache(definitions)),
          removeWhole(events),
          removePartial(events),
          cleanupOutdatedReferences,
          newEntities(events, nextId)
      ))
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(bulletState: BulletState, animationDurations: AnimationDurationMap, playerCommands: Commands,
                definitions: Definitions, events: Events, delta: Float): (World) -> World =
    pipe(listOf(
        { world ->
          val intermediate = generateIntermediateRecords(bulletState, definitions, world, playerCommands, events)
          val linearForces = allCharacterMovements(world, intermediate.commands)
          pipe(listOf(
              updateBulletPhysics(bulletState, linearForces),
              updateWorldDeck(animationDurations, definitions, intermediate, delta)
          ))(world)
        },
        updateGlobalDetails,
        updateBuffUpdateCounter
    ))
