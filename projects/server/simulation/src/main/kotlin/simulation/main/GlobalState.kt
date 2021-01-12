package simulation.main

import silentorb.mythic.ent.LooseGraph
import silentorb.mythic.happenings.Events
import simulation.misc.*

data class GlobalState(
    val doom: Long,
    val frame: Long,
    val gameOver: GameOver?
)

fun newGlobalState(): GlobalState =
    GlobalState(
        doom = 0L,
        frame = 0L,
        gameOver = null
    )

fun checkGameOver(deck: Deck, graph: LooseGraph): GameOver? {
  val victoryLocations = getNodePositionsByAttribute(graph, GameAttributes.victoryZone).map { it.translation() }
  val playersInRange = deck.players.keys
      .mapNotNull { deck.bodies[it] }
      .any { body ->
        victoryLocations.any { it.distance(body.position) < 8f }
      }
  return if (playersInRange)
    GameOver(
        winningFaction = Factions.misfits
    )
  else
    null
}

const val doomInterval = 60 * 5

fun updateDoom(frame: Long, doom: Long): Long {
  val interval = (frame % doomInterval)
  return if (interval == doomInterval - 1L)
    doom + 1L
  else
    doom
}

fun updateGlobalState(deck: Deck, graph: LooseGraph, state: GlobalState): GlobalState {
  return GlobalState(
      doom = updateDoom(state.frame, state.doom),
      frame = state.frame + 1L,
      gameOver = checkGameOver(deck, graph)
  )
}
