package junk_client.views

import junk_client.CommandType
import junk_simulation.World
import mythic.bloom.*
import mythic.spatial.Vector2

data class ClientBattleState(
    val placeholder: Int = 1
)

fun playerView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
  return listOf(label("Player", bounds))
}

fun battleView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
  val columnLengths = resolveLengths(bounds.dimensions.x, listOf(100f, null, 100f))
  val columns = listBounds(horizontalPlane, Vector2(), bounds, columnLengths)

  return listOf<Box>()
      .plus(playerView(state, world, columns[2]))
}
