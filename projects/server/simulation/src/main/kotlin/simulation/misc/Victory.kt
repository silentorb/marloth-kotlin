package simulation.misc

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.main.World

data class GameOver(
    val winningFaction: Id
)

data class VictoryKeyStats(
    val collected: Int,
    val total: Int,
    val remaining: Int
)

fun getVictoryKeyStats(grid: MapGrid, deck: Deck): VictoryKeyStats {
  val victoryKeys = getAllVictoryKeys(deck.accessories)
  val collectedKeyCount = victoryKeys.count { (id, accessory) -> isAtHome(grid, deck)(id) && accessory.owner == 0L }
  val total = victoryKeys.size

  return VictoryKeyStats(
      collected = collectedKeyCount,
      total = total,
      remaining = total - collectedKeyCount
  )
}

fun isVictory(world: World): Boolean {
  if (getDebugString("DISABLE_VICTORY") != null) return false

  val deck = world.deck
  val grid = world.realm.grid

  val victoryKeyStats = getVictoryKeyStats(grid, deck)

  // Prevent victory when there are no victory keys.
  return victoryKeyStats.total > 0 && victoryKeyStats.remaining == 0
}
