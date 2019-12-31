package simulation.updating

import silentorb.mythic.combat.general.getDamageMultiplierModifiers
import silentorb.mythic.combat.general.updateDestructibleCache
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.pipe
import silentorb.mythic.ent.pipe2
import silentorb.mythic.happenings.Events
import simulation.combat.toCombatDefinitions
import simulation.combat.toModifierDeck
import simulation.main.*
import simulation.misc.Definitions
import simulation.physics.updatePhysics

const val simulationFps = 60
const val simulationDelta = 1f / simulationFps.toFloat()

fun updateDeckCache(definitions: Definitions): (Deck) -> Deck =
    { deck ->
      val combatDefinitions = toCombatDefinitions(definitions)
      val damageModifierQuery = getDamageMultiplierModifiers(combatDefinitions, toModifierDeck(deck))
      deck.copy(
          destructibles = mapTable(deck.destructibles, updateDestructibleCache(definitions.damageTypes, damageModifierQuery))
      )
    }

fun updateDeck(definitions: Definitions, events: Events, world: World,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(definitions, world, events),
        ifUpdatingLogic(world.deck, updateDeckCache(definitions)),
        removeWhole(world.definitions.soundDurations, events, world.deck),
        removePartial(events, world.deck),
        cleanupOutdatedReferences,
        newEntities(definitions, world.deck, events, nextId)
    )

fun updateWorldDeck(definitions: Definitions, events: Events, delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val newDeck = updateDeck(definitions, events, world, nextId)(world.deck)
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(definitions: Definitions, previous: Deck, externalEvents: Events,
                delta: Float): (World) -> World =
    pipe2(listOf(
        { world ->
          val events = generateIntermediateRecords(definitions, previous, world, externalEvents)
          pipe2(listOf(
              updatePhysics(events),
              updateWorldDeck(definitions, events, delta)
          ))(world)
        },
        updateGlobalDetails
    ))
