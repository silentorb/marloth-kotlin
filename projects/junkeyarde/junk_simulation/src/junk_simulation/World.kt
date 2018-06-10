package junk_simulation

private var idCounter: Long = 0

fun resetId() {
  idCounter = 0
}
fun nextId(): Long {
  return idCounter++
}

data class World(
    val turn: Int,
    val wave: Int,
    val characters: Map<Id, Character>
) {
  val player: Character
    get() = characters.values.first { it.type.category == CharacterCategory.player }
}

