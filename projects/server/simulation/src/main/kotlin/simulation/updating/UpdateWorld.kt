package simulation.updating

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.applyBodyChanges
import simulation.characters.newMoveSpeedTable
import simulation.combat.general.getDamageMultiplierModifiers
import simulation.combat.general.updateDestructibleCache
import simulation.combat.toCombatDefinitions
import simulation.combat.toModifierDeck
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.updateNavigation
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
               navigation: NavigationState,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(definitions, world, navigation, events),
        ifUpdatingLogic(world.deck, updateDeckCache(definitions)),
        removeWhole(world.definitions.soundDurations, events, world.deck),
        removePartial(events, world.deck),
        cleanOutdatedReferences,
        newEntities(definitions, world.realm.grid, world.deck, events, nextId)
    )

fun updateWorldDeck(definitions: Definitions, navigation: NavigationState, events: Events, delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val newDeck = updateDeck(definitions, events, world, navigation, nextId)(world.deck)
      finalize(world.copy(
          deck = newDeck
      ))
    }

fun updateWorld(definitions: Definitions, events: Events, delta: Float, world: World): World {
  val withPhysics = updatePhysics(events)(world)
  val moveSpeedTable = newMoveSpeedTable(definitions, withPhysics.deck)
  val navigation = updateNavigation(withPhysics.deck, moveSpeedTable, delta, world.navigation!!)
  val next = updateGlobalDetails(updateWorldDeck(definitions, navigation, events, delta)(withPhysics))
  applyBodyChanges(world.bulletState, withPhysics.deck.bodies, next.deck.bodies)
  return next.copy(
      navigation = navigation
  )
}
