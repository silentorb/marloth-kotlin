package simulation

import intellect.Spirit
import physics.Body
import scenery.Depiction
import scenery.Light
import simulation.combat.Missile

val maxPlayerCount = 4

typealias Players = List<Player>
typealias IdentityMap<T> = Map<Id, T>
typealias BodyTable = Map<Id, Body>
typealias CharacterTable = Map<Id, Character>

data class World(
    val bodies: List<Body> = listOf(),
    val characters: List<Character> = listOf(),
    val animations: List<ArmatureAnimation> = listOf(),
    val factions: List<Faction>,
//    val lights: List<Light> = listOf(),
    val meta: AbstractWorld,
    val missiles: List<Missile> = listOf(),
    val nextId: Id,
    val players: List<Player> = listOf(),
    val spirits: List<Spirit> = listOf()
) {
  fun plus(other: NewEntitiesWorld) = this.copy(
      bodies = bodies.plus(other.bodies),
      characters = characters.plus(other.characters),
      players = players.plus(other.players),
      missiles = missiles.plus(other.missiles),
      spirits = spirits.plus(other.spirits)
  )
}

data class NewEntitiesWorld(
    val bodies: List<Body>,
    val characters: List<Character>,
    val missiles: List<Missile>,
    val players: List<Player>,
    val spirits: List<Spirit>
)

data class WorldMap(
    val bodyTable: BodyTable,
    val characterTable: CharacterTable,
    val animationTable: IdentityMap<ArmatureAnimation>,
//    val lights: Map<Id, Light>,
    val missileTable: Map<Id, Missile>,
    val spiritTable: Map<Id, Spirit>,
    val state: World
) {

   val meta: AbstractWorld
    get() = state.meta

  val players: List<Player>
    get() = state.players

  val characters: Collection<Character>
    get() = characterTable.values

  val bodies: Collection<Body>
    get() = bodyTable.values

  val missiles: Collection<Missile>
    get() = missileTable.values

  val spirits: Collection<Spirit>
    get() = spiritTable.values
}

fun <T : EntityLike> mapEntities(list: Collection<T>): Map<Id, T> =
    list.associate { Pair(it.id, it) }

fun generateWorldMap(world: World): WorldMap =
    WorldMap(
        bodyTable = mapEntities(world.bodies),
        characterTable = mapEntities(world.characters),
        animationTable = world.animations.associate { Pair(it.id, it) },
//        lights = world.lights.associate { Pair(it.id, it) },
        missileTable = mapEntities(world.missiles),
        spiritTable = mapEntities(world.spirits),
        state = world

    )