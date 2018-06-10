package junk_simulation

import junk_simulation.data.Characters

fun initializeAbility(type: AbilityType, level: Int): Ability {
  return Ability(
      id = nextId(),
      type = type,
      level = level,
      cooldown = 0
  )
}

fun newCharacter(type: CharacterType): Character {
  return Character(
      id = nextId(),
      type = type,
      level = type.level,
      abilities = type.abilities.map { initializeAbility(it.first, it.second) },
      resources = listOf()
  )
}

fun newPlayer(): Character =
    newCharacter(Characters.player)

fun newWorld(): World {
  resetId()
  val player = newPlayer()
  return World(
      turn = 1,
      wave = 1,
      characters = mapOf(
          player.id to player
      )
  )
}