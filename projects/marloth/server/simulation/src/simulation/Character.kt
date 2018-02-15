package simulation

class Character(
    val id: Int,
    val body: Body
) {
  val abilities: MutableList<Ability> = mutableListOf()
}