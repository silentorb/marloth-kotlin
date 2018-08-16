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
    val character = world.characterTable[spirit.id]!!
    val body = world.bodyTable[spirit.id]!!
    val data = if (!spiritMovementTallies.containsKey(spirit.id))
      Pair(0f, body.position)
    else {
      val previous = spiritMovementTallies[character.id]!!
      val tally = if (body.position != previous.second)
        0f
      else
        previous.first + delta

      Pair(tally, body.position)
    }
    Pair(character.id, data)
  }

  for (spirit in world.spirits) {
    val character = world.characterTable[spirit.id]!!
    val data = spiritMovementTallies[character.id]!!
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