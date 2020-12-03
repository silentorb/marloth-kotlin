package simulation.characters

import simulation.misc.HighInt
import simulation.misc.Int1000
import simulation.misc.highIntScale
import simulation.misc.intMinute
import kotlin.math.max

private var feetTravelled1000: Int = 0

fun updateNourishment(frames: Int, character: Character, velocity: Int1000): HighInt {
  val previous = character.nourishment
  return if (frames < 1)
    previous
  else {
    feetTravelled1000 += velocity / 60
    val delta = highIntScale * frames
    val movementCostPerFoot = highIntScale / 1000
    val timeDrainDuration = intMinute * 30
    val movementCost = movementCostPerFoot * frames * velocity / 1000 / 60
    val timeCost = delta / timeDrainDuration
    val cost = movementCost + timeCost
    max(0, previous - cost)
  }
}
