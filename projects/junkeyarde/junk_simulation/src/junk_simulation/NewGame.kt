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

fun newPlayer(playerAbilities: List<AbilityType>): Character =
    newCharacter(Characters.player)
        .copy(
            abilities = playerAbilities.map { initializeAbility(it, 1) }
        )

fun newWorld(playerAbilities: List<AbilityType>): World {
  resetId()
  val player = newPlayer(playerAbilities)
  return World(
      round = 1,
      wave = 1,
      characters = mapOf(
          player.id to player
      ),
      turns = listOf()
  )
}