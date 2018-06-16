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

val isPlayer = { it: Creature -> it.type.category == CreatureCategory.player }

data class World(
    val round: Int,
    val wave: Int,
    val creatures: CreatureMap,
    val turns: List<Id>,
    val animation: Animation?
) {
  val player: Creature
    get() = creatures.values.first(isPlayer)

  val enemies: List<Creature>
    get() = creatures.values.filter { it.type.category == CreatureCategory.enemy }

  val activeCreatureId: Id =
      turns.first()

  val activeCreature: Creature =
      creatures [turns.first()]!!
}

