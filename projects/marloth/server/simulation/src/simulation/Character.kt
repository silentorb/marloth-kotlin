package simulation

class Character(
    val id: Int,
    val body: Body,
    maxHealth: Int
) {
  val health = Resource(maxHealth)
  val abilities: MutableList<Ability> = mutableListOf()
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0