package simulation.characters

import simulation.misc.HighInt
import simulation.misc.Int1000
import simulation.misc.highIntScale
import simulation.misc.intMinute

val standardEnergyRates = ResourceRates(
    distanceTraveledDrainDuration = 1000,
    timeDrainDuration = intMinute * 30,
)

val shadowSpiritEnergyRates = ResourceRates(
    distanceTraveledDrainDuration = 500,
    timeDrainDuration = intMinute * 30,
)

const val healthTimeDrainDuration = intMinute * 30

fun getNourishmentExpense(timeDrainDuration: Int, frames: Int): HighInt {
  val delta = highIntScale * frames * 100
  return delta / timeDrainDuration
}

fun getEnergyExpense(rates: ResourceRates, frames: Int, velocity: Int1000): HighInt {
  val distanceTraveledDrainDuration = rates.distanceTraveledDrainDuration
  val movementCostPerFoot = highIntScale / distanceTraveledDrainDuration
  val movementCost = movementCostPerFoot * frames * velocity / 1000 / 60
  val timeCost = getNourishmentExpense(rates.timeDrainDuration, frames)
  return movementCost + timeCost
}

fun getRoundedAccumulation(expense: HighInt): Int =
    expense / highIntScale

//fun applyAccumulativeExpense(value: Int, expense: HighInt): Pair<Int, HighInt> {
//  val mod = expense / highIntScale
//  return Pair(value - mod, expense - mod)
//}

//fun updateEnergy(character: Character, velocity: Int1000, commands: Commands): HighInt {
//  val adjustment = 0
//  return if (commands.any { it.type == CharacterCommands.sleep })
//    highIntScale
//  else
//    updateEnergy(1, velocity, adjustment, character.energy)
//}

