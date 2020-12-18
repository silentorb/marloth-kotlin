package simulation.characters

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.happenings.Commands
import simulation.misc.*
import kotlin.math.min

val energyRates = ResourceRates(
    distanceTraveledDrainDuration = 500,
    timeDrainDuration = intMinute * 15,
)

val updateEnergy = updatePercentageResource(energyRates)

fun updateEnergy(character: Character, velocity: Int1000, commands: Commands): HighInt {
  val adjustment = 0
  return if (commands.any { it.type == CharacterCommands.sleep })
    highIntScale
  else
    updateEnergy(1, velocity, adjustment, character.energy)
}
