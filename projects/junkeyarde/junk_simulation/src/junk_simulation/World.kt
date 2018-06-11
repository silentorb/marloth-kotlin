package junk_simulation

private var idCounter: Long = 0

fun resetId() {
  idCounter = 0
}

fun nextId(): Long {
  // Double the amount of valid id values by dipping into the negatives when necessary
  if (idCounter == Long.MAX_VALUE - 1) {
    idCounter = Long.MIN_VALUE
    return Long.MAX_VALUE
  }

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

