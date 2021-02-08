package simulation.main

import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Commands
import simulation.intellect.navigation.NavigationState
import silentorb.mythic.randomly.Dice
import simulation.misc.Definitions
import simulation.misc.Realm
import silentorb.mythic.physics.BulletState
import simulation.misc.GameModeConfig

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val global: GlobalState,
    val dice: Dice,
    val availableIds: Set<Id>,
    val navigation: NavigationState?,
    val staticGraph: Graph = newGraph(),
    val bulletState: BulletState,
    val definitions: Definitions,
    val gameModeConfig: GameModeConfig,
    val persistence: Graph,
    val graph: GraphStore,
    val step: Long,
    val nextCommands: Commands = listOf(),
//    val requests: Requests = listOf(),
)

typealias WorldPair = Pair<World, World>

fun newIdSourceFromWorld(world: World): Pair<IdSource, (World) -> World> {
  var availableIds = world.availableIds
  var nextId = world.nextId
  return Pair({
    if (availableIds.any()) {
      val result = availableIds.last()
      availableIds = availableIds.minus(result)
      result
    } else {
      nextId++
    }
  }, { newWorld ->
    newWorld.copy(
        availableIds = availableIds,
        nextId = nextId
    )
  })
}
