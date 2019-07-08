package simulation.main

import simulation.misc.GameOver
import simulation.misc.isVictory
import simulation.misc.misfitsFaction

val updateGlobalDetails: (World) -> World = { world ->
  if (world.gameOver == null && isVictory(world))
    world.copy(
        gameOver = GameOver(
            winningFaction = misfitsFaction
        )
    )
  else
    world
}

// Set to update once per second with a fixed 60 frames per second
val updateBuffUpdateCounter: (World) -> World = { world ->
  world.copy(
      logicUpdateCounter = (world.logicUpdateCounter + 1) % 60
  )
}
