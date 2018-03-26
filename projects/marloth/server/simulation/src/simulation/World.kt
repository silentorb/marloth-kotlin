package simulation

import intellect.Spirit
import scenery.Depiction
import scenery.Light

val maxPlayerCount = 4

typealias Players = List<Player>
typealias IdentityMap<T> = MutableMap<Id, T>

data class World(
    val meta: AbstractWorld
) {
  val entities: MutableMap<Id, Entity> = mutableMapOf()
  val players: MutableList<Player> = mutableListOf()
  val bodyTable: MutableMap<Id, Body> = mutableMapOf()
  val characterTable: MutableMap<Id, Character> = mutableMapOf()
  val missileTable: MutableMap<Id, Missile> = mutableMapOf()
  val spiritTable: MutableMap<Id, Spirit> = mutableMapOf()
  val depictionTable: IdentityMap<Depiction> = mutableMapOf()

  val factions = mutableListOf(
      Faction(this, "Misfits"),
      Faction(this, "Monsters")
  )
  val lights: MutableMap<Id, Light> = mutableMapOf()

  private var _nextId = 1

  fun getAndSetNextId() = _nextId++

  val characters: MutableCollection<Character>
    get() = characterTable.values

  val bodies: MutableCollection<Body>
    get() = bodyTable.values

  val missiles: MutableCollection<Missile>
    get() = missileTable.values

  val spirits: MutableCollection<Spirit>
    get() = spiritTable.values

  val depictions: MutableCollection<Depiction>
    get() = depictionTable.values
}