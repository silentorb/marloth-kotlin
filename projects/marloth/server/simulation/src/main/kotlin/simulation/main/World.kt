package simulation.main

import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.Table
import mythic.ent.pass
import randomly.Dice
import simulation.entities.Character
import simulation.misc.GameOver
import simulation.entities.Player
import simulation.misc.Realm
import simulation.physics.Body

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val dice: Dice,
    val availableIds: Set<Id>,
    val gameOver: GameOver? = null,
    val logicUpdateCounter: Int
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