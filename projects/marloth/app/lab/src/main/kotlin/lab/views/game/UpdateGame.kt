package lab.views.game

import commanding.CommandType
import haft.Commands
import lab.LabApp
import mythic.spatial.Vector3
import simulation.Id
import simulation.World
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig
import simulation.changing.WorldUpdater

const val nSecond: Long = 1000000000L
const val maxInterval = 1f / 60f

private var spiritMovementTallies = mapOf<Id, Pair<Float, Vector3>>()

fun trackSpiritMovement(world: World, delta: Float) {
  spiritMovementTallies = world.spirits.associate { spirit ->
    val data = if (!spiritMovementTallies.containsKey(spirit.character.id))
      Pair(0f, spirit.body.position)
    else {
      val previous = spiritMovementTallies[spirit.character.id]!!
      val tally = if (spirit.body.position != previous.second)
        0f
      else
        previous.first + delta

      Pair(tally, spirit.body.position)
    }
    Pair(spirit.character.id, data)
  }

  for (spirit in world.spirits) {
    val data = spiritMovementTallies[spirit.character.id]!!
    if (data.first > 10f) {
      val k = 0
    }
  }
}

fun updateWorld(app: LabApp, commands: Commands<CommandType>, delta: Float) {
  if (app.config.gameView.logDroppedFrames && app.timer.actualDelta > maxInterval) {
    val progress = app.timer.last - app.timer.start
    println("" + (progress.toDouble() / nSecond.toDouble()).toFloat() + ": " + app.timer.actualDelta)
  }
  val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
  val updater = WorldUpdater(app.world, instantiator)
  updater.update(commands, delta)
//  trackSpiritMovement(app.world, delta)
}