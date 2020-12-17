package simulation.characters

import simulation.misc.*
import kotlin.math.max
import kotlin.math.min

data class ResourceRates(
    val distanceTraveledDrainDuration: Int, // With no other factors, moving X feet will deplete full nourishment
    val timeDrainDuration: Int, // With no other factors, waiting X frames will deplete full nourishment
)

fun updatePercentageResource(rates: ResourceRates, frames: Int, velocity: Int1000, value: HighInt): HighInt {
  val delta = highIntScale * frames
  val distanceTraveledDrainDuration = rates.distanceTraveledDrainDuration
  val timeDrainDuration = rates.timeDrainDuration
  val movementCostPerFoot = highIntScale / distanceTraveledDrainDuration
  val movementCost = movementCostPerFoot * frames * velocity / 1000 / 60
  val timeCost = delta / timeDrainDuration
  val cost = movementCost + timeCost
  return max(0, value - cost)
}

fun updatePercentageResource(rates: ResourceRates): (Int, Int1000, Int, HighInt) -> HighInt =
    { frames, velocity, adjustment, value ->
      val adjusted = if (adjustment == 0)
        value
      else
        min(highIntScale, value + percentageToHighInt(adjustment))

      if (frames < 1)
        adjusted
      else {
        updatePercentageResource(rates, frames, velocity, adjusted)
      }
    }
