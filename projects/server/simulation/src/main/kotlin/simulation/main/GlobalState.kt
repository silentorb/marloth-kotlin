package simulation.main

import simulation.misc.GameOver
import simulation.misc.MapGrid
import simulation.misc.isVictory
import simulation.misc.misfitFaction

data class GlobalState(
    val frame: Long,
    val gameOver: GameOver?
)

fun newGlobalState(): GlobalState =
    GlobalState(
        frame = 0L,
        gameOver = null
    )

fun updateGlobalState(deck: Deck, grid: MapGrid, state: GlobalState): GlobalState {
  return GlobalState(
      frame = state.frame + 1L,
      gameOver = if (isVictory(deck, grid))
        GameOver(
            winningFaction = misfitFaction
        )
      else
        null
  )
}
