package lab.views.game

import front.mapCommands
import haft.HaftCommands
import lab.LabApp
import marloth.clienting.CommandType
import mythic.spatial.Vector3
import simulation.Id
import simulation.WorldMap
import simulation.changing.updateWorld

const val nSecond: Long = 1000000000L
const val maxInterval = 1f / 60f

private var spiritMovementTallies = mapOf<Id, Pair<Float, Vector3>>()

fun trackSpiritMovement(world: WorldMap, delta: Float) {
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

fun updateLabWorld(app: LabApp, commands: HaftCommands<CommandType>, delta: Float) {
  if (app.config.gameView.logDroppedFrames && app.timer.actualDelta > maxInterval) {
    val progress = app.timer.last - app.timer.start
    println("" + (progress.toDouble() / nSecond.toDouble()).toFloat() + ": " + app.timer.actualDelta)
  }
//  val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
  val world = app.world
  val characterCommands = mapCommands(world.players, commands)
  updateWorld(world, characterCommands, delta)

//  trackSpiritMovement(app.world, delta)
}