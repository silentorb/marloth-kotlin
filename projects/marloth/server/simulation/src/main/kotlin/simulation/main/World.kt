package simulation.main

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.pass
import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.randomly.Dice
import simulation.misc.Definitions
import simulation.misc.GameOver
import simulation.misc.Realm
import silentorb.mythic.physics.BulletState
import simulation.misc.GameModeConfig

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val dice: Dice,
    val availableIds: Set<Id>,
    val gameOver: GameOver? = null,
    val navMesh: NavMesh?,
    val navMeshQuery: NavMeshQuery?,
    val bulletState: BulletState,
    val definitions: Definitions,
    val gameModeConfig: GameModeConfig
)

typealias WorldTransform = (World) -> World

typealias WorldPair = Pair<World, World>

val shouldUpdateLogic = { deck: Deck -> deck.cyclesInt.values.firstOrNull { it.interval == 30 }?.value == 0 }

fun <T> ifUpdatingLogic(deck: Deck, transform: (T) -> T): (T) -> T =
    if (shouldUpdateLogic(deck))
      transform
    else
      ::pass

fun newIdSource(world: World): Pair<IdSource, (World) -> World> {
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
