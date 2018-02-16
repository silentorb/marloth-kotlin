package simulation

class Faction(
    val world: World,
    val name: String
) {

  val characters: List<Character>
    get() = world.characters.filter { it.faction === this }

  val enemies: List<Character>
    get() = world.characters.filter { it.faction !== this }
}