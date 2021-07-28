package simulation.characters

import simulation.misc.intMinute

const val maxSanity = 100

val standardSanityRates = ResourceRates(
    distanceTraveledDrainDuration = 0,
    timeDrainDuration = intMinute * 30,
)
