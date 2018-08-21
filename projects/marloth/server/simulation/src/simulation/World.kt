package simulation

import intellect.Spirit
import physics.Body
import scenery.Depiction
import scenery.Light
import simulation.combat.Missile

val maxPlayerCount = 4

typealias Players = List<Player>
typealias IdentityMap<T> = MutableMap<Id, T>
typealias BodyTable = Map<Id, Body>
typealias CharacterTable = Map<Id, Character>

data class World(
    val meta: AbstractWorld
) {
  var players: List<Player> = listOf()
  var bodyTable: BodyTable = mapOf()
  var characterTable: CharacterTable = mapOf()
  var missileTable: Map<Id, Missile> = mapOf()
  var spiritTable: MutableMap<Id, Spirit> = mutableMapOf()
  val depictionTable: IdentityMap<Depiction> = mutableMapOf()

  val factions = mutableListOf(
      Faction(this, "Misfits"),
      Faction(this, "Monsters")
  )
  val lights: MutableMap<Id, Light> = mutableMapOf()

  private var _nextId = 1

  fun getAndSetNextId() = _nextId++

  val characters: Collection<Character>
    get() = characterTable.values

  val bodies: Collection<Body>
    get() = bodyTable.values

  val missiles: Collection<Missile>
    get() = missileTable.values

  val spirits: MutableCollection<Spirit>
    get() = spiritTable.values

  val depictions: MutableCollection<Depiction>
    get() = depictionTable.values

  val playerCharacters: List<PlayerCharacter>
    get() = players.map { PlayerCharacter(it, characterTable[it.character]!!) }
}