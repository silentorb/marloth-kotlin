package simulation

class Character(
    val id: Int
) {
  val abilities: MutableList<Ability> = mutableListOf()
}