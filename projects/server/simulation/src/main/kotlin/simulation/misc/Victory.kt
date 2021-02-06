package simulation.misc

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import simulation.main.Deck

data class GameOver(
    val winningFaction: Id
)

data class VictoryKeyStats(
    val collected: Int,
    val total: Int,
    val remaining: Int
)

//fun getVictoryKeyStats(grid: MapGrid, deck: Deck): VictoryKeyStats {
//  val victoryKeys = getAllVictoryKeys(deck.accessories)
//  val collectedKeyCount = victoryKeys
//      .count { (id, accessory) ->
//        isAtHome(grid, deck)(id) && accessory.owner == 0L
//      }
//
//  val total = victoryKeys.size
//
//  return VictoryKeyStats(
//      collected = collectedKeyCount,
//      total = total,
//      remaining = total - collectedKeyCount
//  )
//}

//fun isVictory(deck: Deck, grid: MapGrid): Boolean {
//  if (getDebugString("DISABLE_VICTORY") != null) return false
//
//  val victoryKeyStats = getVictoryKeyStats(grid, deck)
//
//  // Prevent victory when there are no victory keys.
//  return victoryKeyStats.total > 0 && victoryKeyStats.remaining == 0
//}
