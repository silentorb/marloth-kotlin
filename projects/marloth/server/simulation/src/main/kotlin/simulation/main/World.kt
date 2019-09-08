package simulation.main

import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.pass
import org.recast4j.detour.NavMesh
import randomly.Dice
import simulation.misc.GameOver
import simulation.misc.Realm

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val dice: Dice,
    val availableIds: Set<Id>,
    val gameOver: GameOver? = null,
    val logicUpdateCounter: Int,
    val navMesh: NavMesh
)

typealias WorldTransform = (World) -> World

typealias WorldPair = Pair<World, World>

val shouldUpdateLogic = { world: World -> world.logicUpdateCounter == 0 }

fun <T> ifUpdatingLogic(world: World, transform: (T) -> T): (T) -> T =
    if (shouldUpdateLogic(world))
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
