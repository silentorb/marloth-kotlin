package simulation.characters

import simulation.misc.*
import kotlin.math.min

val energyRates = ResourceRates(
    distanceTraveledDrainDuration = 500,
    timeDrainDuration = intMinute * 15,
)

val updateEnergy = updatePercentageResource(energyRates)
