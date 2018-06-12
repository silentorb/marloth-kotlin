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
    val creatures: Map<Id, Creature>,
    val turns: List<Int>
) {
  val player: Creature
    get() = creatures.values.first { it.type.category == CreatureCategory.player }

  val enemies: List<Creature>
    get() = creatures.values.filter { it.type.category == CreatureCategory.enemy }
}

