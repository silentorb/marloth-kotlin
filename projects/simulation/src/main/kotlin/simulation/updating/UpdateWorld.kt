package simulation.updating

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.applyBodyChanges
import simulation.characters.newMoveSpeedTable
import simulation.happenings.gatherNextCommands
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.updateNavigation
import simulation.macro.applyMacroUpdates
import simulation.main.Deck
import simulation.main.Frames
import simulation.main.World
import simulation.main.updateGlobalState
import simulation.misc.Definitions
import simulation.physics.updatePhysics
import kotlin.math.max

const val simulationFps: Int = 60
const val simulationDelta: Float = 1f / simulationFps.toFloat()
const val nanosecondsInSecond = 1_000_000_000
const val simulationNanoseconds = nanosecondsInSecond / simulationFps

fun getIdle(increment: Long): Long =
    max(0L, simulationNanoseconds - increment)

fun updateDeck(definitions: Definitions, events: Events, world: World,
               navigation: NavigationState?,
               nextId: IdSource): (Deck) -> Deck =
    pipe(
        updateEntities(definitions, world, navigation, events),
        removeWhole(world.definitions.soundDurations, events, world.deck),
        cleanOutdatedReferences,
        newEntities(definitions, world.staticGraph.value, world.step, world.deck, events, nextId)
    )

fun updateWorld(events: Events, frames: Frames, world: World): World {
  val delta = simulationDelta
  val withPhysics = updatePhysics(events)(world)
  val moveSpeedTable = newMoveSpeedTable(world.definitions, withPhysics.deck)
  val navigation = if (withPhysics.navigation != null)
    updateNavigation(withPhysics.deck, moveSpeedTable, delta, withPhysics.navigation)
  else
    null

  val nextId = withPhysics.nextId.source()
  val deck = updateDeck(world.definitions, events, withPhysics, navigation, nextId)(withPhysics.deck)
  applyBodyChanges(withPhysics.bulletState, withPhysics.deck.bodies, deck.bodies)

  val postWorld = withPhysics.copy(
      deck = deck,
      global = updateGlobalState(deck, world.staticGraph.value, withPhysics.global),
      navigation = navigation,
      nextCommands = gatherNextCommands(world, events),
      step = world.step + 1L,
  )

  return applyMacroUpdates(events.filterIsInstance<Command>(), postWorld)
}
