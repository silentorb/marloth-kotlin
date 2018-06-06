package junk_simulation

data class World(
    val turn: Int,
    val wave: Int,
    val characters: Map<Id, Character>
)

