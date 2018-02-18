package simulation

import mythic.spatial.Vector3

data class CharacterDefinition(
    val health: Int,
    val abilities: List<AbilityDefinition>
)

class Character(
    val id: Int,
    val body: Body,
    maxHealth: Int,
    val abilities: MutableList<Ability> = mutableListOf(),
    val faction: Faction
) {
  val health = Resource(maxHealth)
  var isAlive = true
  var rotation: Vector3 = Vector3()
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0