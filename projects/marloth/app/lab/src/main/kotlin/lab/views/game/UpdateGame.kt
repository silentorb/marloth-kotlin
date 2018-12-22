package lab.views.game

import mythic.ent.Id
import mythic.spatial.Vector3

const val nSecond: Long = 1000000000L
const val maxInterval = 1f / 60f

private var spiritMovementTallies = mapOf<Id, Pair<Float, Vector3>>()

//fun trackSpiritMovement(world: World, delta: Float) {
//  spiritMovementTallies = world.spirits.associate { spirit ->
//    val child = world.characterTable[spirit.id]!!
//    val body = world.bodyTable[spirit.id]!!
//    val data = if (!spiritMovementTallies.containsKey(spirit.id))
//      Pair(0f, body.position)
//    else {
//      val previous = spiritMovementTallies[child.id]!!
//      val tally = if (body.position != previous.second)
//        0f
//      else
//        previous.first + delta
//
//      Pair(tally, body.position)
//    }
//    Pair(child.id, data)
//  }
//
//  for (spirit in world.spirits) {
//    val child = world.characterTable[spirit.id]!!
//    val data = spiritMovementTallies[child.id]!!
//    if (data.first > 10f) {
//      val k = 0
//    }
//  }
//}

//fun updateLabWorld(app: LabApp, commands: HaftCommands<CommandType>, delta: Float): World? {
////  if (app.config.gameView.logDroppedFrames && app.timer.actualDelta > maxInterval) {
////    val progress = app.timer.last - app.timer.start
////    println("" + (progress.toDouble() / nSecond.toDouble()).toFloat() + ": " + app.timer.actualDelta)
////  }
////  val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
//  val world = app.world
//  return if (world != null) {
//    val characterCommands = mapCommands(world.players, commands)
//    updateWorld(app.client.renderer.animationDurations, world.deck, world, characterCommands, delta)
//  } else
//    null
////  trackSpiritMovement(app.world, delta)
//}