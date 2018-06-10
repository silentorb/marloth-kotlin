package junk_simulation

private var idCounter: Long = 0

fun resetId() {
  idCounter = 0
}
fun nextId(): Long {
  return idCounter++
}

data class World(
    val round: Int,
    val wave: Int,
    val characters: Map<Id, Character>,
    val turns: List<Int>
) {
  val player: Character
    get() = characters.values.first { it.type.category == CharacterCategory.player }
}

