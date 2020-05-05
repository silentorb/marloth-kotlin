package simulation.updating

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.newIdSource
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
        removeWhole(world.definitions.soundDurations, events, world.deck),
        removePartial(events, world.deck),
        cleanOutdatedReferences,
        newEntities(definitions, world.realm.grid, world.deck, events, nextId)
    )

fun updateWorld(definitions: Definitions, events: Events, delta: Float, world: World): World {
  val withPhysics = updatePhysics(events)(world)
  val moveSpeedTable = newMoveSpeedTable(definitions, withPhysics.deck)
  val navigation = updateNavigation(withPhysics.deck, moveSpeedTable, delta, withPhysics.navigation!!)
  val nextId = newIdSource(withPhysics.nextId)
  val deck = updateDeck(definitions, events, withPhysics, navigation, nextId)(withPhysics.deck)
  applyBodyChanges(withPhysics.bulletState, withPhysics.deck.bodies, deck.bodies)

  return withPhysics.copy(
      deck = deck,
      global = updateGlobalState(deck, withPhysics.realm.grid, withPhysics.global),
      navigation = navigation,
      nextId = nextId()
  )
}
