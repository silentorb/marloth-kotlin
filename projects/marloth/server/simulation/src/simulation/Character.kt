package simulation

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
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0