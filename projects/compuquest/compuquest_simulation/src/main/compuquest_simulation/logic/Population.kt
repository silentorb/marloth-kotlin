package compuquest_simulation.logic

import compuquest_simulation.CreatureType
import compuquest_simulation.Creature
import compuquest_simulation.data.creatureLibrary
import compuquest_simulation.newCreature
import silentorb.mythic.randomly.Dice

typealias WavePopulation = List<CreatureType>

val waveCount = 6

fun populateWave(creatures: List<CreatureType>, wave: Int): WavePopulation {
  return creatures.flatMap { creatureType ->
    val gap = Math.abs(creatureType.level - wave)
    val quantity = Math.max(0, creatureType.frequency - gap)
    (0 until quantity).map { creatureType }
  }
}

fun populateWaves(): List<WavePopulation> =
    (1..waveCount).map { populateWave(creatureLibrary, it) }

val wavePopulations = populateWaves()

fun newEnemy(wave: Int): Creature {
  val population = wavePopulations[wave - 1]
  val type = Dice.global.takeOne(population)
  return newCreature(type)
}

fun newEnemies(wave: Int, count: Int): List<Creature> =
    (0 until count).map { newEnemy(wave) }
