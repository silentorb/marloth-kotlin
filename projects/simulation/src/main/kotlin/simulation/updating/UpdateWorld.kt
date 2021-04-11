package simulation.updating

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.applyBodyChanges
import simulation.characters.newMoveSpeedTable
import simulation.combat.general.getDamageMultiplierModifiers
import simulation.combat.general.updateDestructibleCache
import simulation.combat.toCombatDefinitions
import simulation.combat.toModifierDeck
import simulation.entities.remapPlayerRigCommands
import simulation.happenings.gatherNextCommands
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.updateNavigation
import simulation.main.*
import simulation.misc.Definitions
import simulation.physics.updatePhysics
import kotlin.math.max

const val simulationFps: Int = 60
const val simulationDelta: Float = 1f / simulationFps.toFloat()
const val nanosecondsInSecond = 1_000_000_000
const val simulationNanoseconds = nanosecondsInSecond / simulationFps

fun getIdle(increment: Long): Long =
    max(0L, simulationNanoseconds - increment)

fun updateDeckCache(definitions: Definitions): (Deck) -> Deck =
    { deck ->
      val combatDefinitions = toCombatDefinitions(definitions)
      val damageModifierQuery = getDamageMultiplierModifiers(combatDefinitions, toModifierDeck(deck))
      deck.copy(
          destructibles = mapTable(deck.destructibles, updateDestructibleCache(definitions.damageTypes, damageModifierQuery))
      )
    }

fun updateDeck(definitions: Definitions, events: Events, world: World,
               navigation: NavigationState?,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(definitions, world, navigation, events),
        removeWhole(world.definitions.soundDurations, events, world.deck),
//        removePartial(events, world.deck),
        cleanOutdatedReferences,
        newEntities(definitions, world.staticGraph, world.step, world.deck, events, nextId)
    )

fun updateWorld(definitions: Definitions, events: Events, commands: Commands, delta: Float, world: World): World {
  val remappedEvents = remapPlayerRigCommands(world.deck.players, events)
  val withPhysics = updatePhysics(remappedEvents)(world)
  val moveSpeedTable = newMoveSpeedTable(definitions, withPhysics.deck)
  val navigation = if (withPhysics.navigation != null)
    updateNavigation(withPhysics.deck, moveSpeedTable, delta, withPhysics.navigation)
  else
    null

  val nextId = withPhysics.nextId.source()
  val deck = updateDeck(definitions, remappedEvents, withPhysics, navigation, nextId)(withPhysics.deck)
  applyBodyChanges(withPhysics.bulletState, withPhysics.deck.bodies, deck.bodies)

  return withPhysics.copy(
      deck = deck,
      global = updateGlobalState(deck, world.staticGraph, withPhysics.global),
      navigation = navigation,
      nextCommands = gatherNextCommands(world, commands),
      step = world.step + 1L,
  )
}
