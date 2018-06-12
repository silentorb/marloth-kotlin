package junk_simulation

import junk_simulation.data.Creatures
import junk_simulation.logic.allocateResources
import junk_simulation.logic.convertSimpleResource
import junk_simulation.logic.newEnemies
import randomly.Dice

fun initializeAbility(type: AbilityType, level: Int): Ability {
  return Ability(
      id = nextId(),
      type = type,
      level = level,
      cooldown = 0
  )
}

fun newResource(element: Element, max: Int) =
    Resource(
        element = element,
        max = max,
        value = max
    )

fun newCreature(type: CreatureType): Creature {
  return Creature(
      id = nextId(),
      type = type,
      level = type.level,
      abilities = type.abilities.map { initializeAbility(it.first, it.second) },
      resources = type.elements.map { newResource(it.key, it.value) }
  )
}

fun newPlayer(playerAbilities: List<AbilityType>): Creature {
  val creatureLevel = 1
  val abilities = playerAbilities.map { initializeAbility(it, creatureLevel) }
  return newCreature(Creatures.player)
      .copy(
          abilities = abilities,
          resources = allocateResources(creatureLevel, abilities).map(convertSimpleResource)
      )
}

fun randomEnemies(min: Int, max: Int, wave: Int) =
    newEnemies(wave, Dice.global.getInt(min, max)).map { Pair(it.id, it) }

fun enemiesAtWaveStart(wave: Int) =
    randomEnemies(1, 2, wave)

fun newWorld(playerAbilities: List<AbilityType>): World {
  resetId()
  val player = newPlayer(playerAbilities)
  val wave = 1
  return World(
      round = 1,
      wave = wave,
      creatures = mapOf(
          player.id to player
      ).plus(enemiesAtWaveStart(wave)),
      turns = listOf()
  )
}