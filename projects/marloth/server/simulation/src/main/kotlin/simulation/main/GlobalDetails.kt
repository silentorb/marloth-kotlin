package simulation.main

import simulation.misc.GameOver
import simulation.misc.isVictory
import simulation.misc.misfitFaction
import simulation.updating.simulationFps

val updateGlobalDetails: (World) -> World = { world ->
  if (world.gameOver == null && isVictory(world))
    world.copy(
        gameOver = GameOver(
            winningFaction = misfitFaction
        )
    )
  else
    world
}

const val updateFrequency = 2
private const val logicUpdateCounterMax = simulationFps / updateFrequency

val updateBuffUpdateCounter: (World) -> World = { world ->
  world.copy(
      logicUpdateCounter = (world.logicUpdateCounter + 1) % logicUpdateCounterMax
  )
}

fun divideUp(dividend: Int, divisor: Int): Int {
  val result = dividend / divisor
  return if (divisor * result != dividend)
    result + 1
  else
    result
}


val overTime: (Int) -> Int = { value ->
  divideUp(value, updateFrequency)
}
