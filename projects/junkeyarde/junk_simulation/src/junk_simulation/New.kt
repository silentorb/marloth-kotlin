package junk_simulation

import junk_simulation.data.Creatures
import junk_simulation.logic.newEnemies
import randomly.Dice

fun initializeAbility(type: AbilityType): Ability {
  return Ability(
      id = nextId(),
      type = type,
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
      abilities = type.abilities.map { initializeAbility(it) },
      life = type.life
//      resources = type.elements.map { newResource(it.key, it.value) }
  )
}

fun newPlayer(playerAbilities: List<AbilityType>): Creature {
  val creatureLevel = 1
  val abilities = playerAbilities.map { initializeAbility(it) }
  return newCreature(Creatures.player)
      .copy(
          abilities = abilities
//          resources = allocateResources(creatureLevel, abilities).map(convertSimpleResource)
      )
}

fun randomEnemies(min: Int, max: Int, wave: Int) =
    newEnemies(wave, Dice.global.getInt(min, max)).map { Pair(it.id, it) }

fun enemiesAtWaveStart(wave: Int) =
    randomEnemies(1, 2, wave)

fun enemiesWhenEmpty(wave: Int) =
    randomEnemies(1, 2, wave)

fun prepareTurns(creatures: Collection<Creature>): List<Id> =
    creatures.filter(isPlayer)
        .plus(creatures.filter { !isPlayer(it) })
        .map { it.id }

fun newWorld(playerAbilities: List<AbilityType>): World {
  resetId()
  val player = newPlayer(playerAbilities)
  val wave = 1
  val creatures = mapOf(
      player.id to player
  ).plus(enemiesAtWaveStart(wave))

  val turns = prepareTurns(creatures.values)
  return World(
      round = 1,
      wave = wave,
      creatures = creatures,
      turns = turns.drop(1),
      animation = null,
      activeCreatureId = turns.first()
  )
}