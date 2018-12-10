package simulation

import intellect.Spirit
import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.entityMap
import physics.Body

typealias BodyTable = IdentityMap<Body>
typealias CharacterTable = IdentityMap<Character>

data class IdentityMap<T>(
    val values: Map<Id, T>
) {
  operator fun get(id: Id): T? = values[id]
}

data class Hand(
    val body: Body? = null,
    val character: Character? = null,
    val animation: ArmatureAnimation? = null,
    val depiction: Depiction? = null,
    val door: Door? = null,
    val light: Light? = null,
    val missile: Missile? = null,
    val player: Player? = null,
    val spirit: Spirit? = null
)

data class Deck(
    val animations: List<ArmatureAnimation> = listOf(),
    val bodies: List<Body> = listOf(),
    val characters: List<Character> = listOf(),
    val factions: List<Faction> = listOf(),
    val depictions: List<Depiction> = listOf(),
    val doors: List<Door> = listOf(),
    val lights: List<Light> = listOf(),
    val missiles: List<Missile> = listOf(),
    val players: List<Player> = listOf(),
    val spirits: List<Spirit> = listOf()
) {
  fun plus(other: Deck) = this.copy(
      animations = animations.plus(other.animations),
      bodies = bodies.plus(other.bodies),
      characters = characters.plus(other.characters),
      factions = factions.plus(other.factions),
      depictions = depictions.plus(other.depictions),
      doors = doors.plus(other.doors),
      lights = lights.plus(other.lights),
      missiles = missiles.plus(other.missiles),
      players = players.plus(other.players),
      spirits = spirits.plus(other.spirits)
  )
}

data class Tables(
    val animations: IdentityMap<ArmatureAnimation>,
    val bodies: IdentityMap<Body>,
    val characters: IdentityMap<Character>,
    val depictions: IdentityMap<Depiction>,
    val doors: IdentityMap<Door>,
    val missiles: IdentityMap<Missile>,
    val spirits: IdentityMap<Spirit>
)

fun <T : Entity> entityMap2(list: Collection<T>): IdentityMap<T> =
    IdentityMap(entityMap(list))

fun toTables(deck: Deck): Tables =
    Tables(
        bodies = entityMap2(deck.bodies),
        characters = entityMap2(deck.characters),
        animations = entityMap2(deck.animations),
        depictions = entityMap2(deck.depictions),
        doors = entityMap2(deck.doors),
        missiles = entityMap2(deck.missiles),
        spirits = entityMap2(deck.spirits)
    )

fun <T> nullableList(entity: T?): List<T> =
    if (entity == null)
      listOf()
    else
      listOf(entity)

fun toDeck(hand: Hand): Deck =
    Deck(
        animations = nullableList(hand.animation),
        bodies = nullableList(hand.body),
        characters = nullableList(hand.character),
        depictions = nullableList(hand.depiction),
        doors = nullableList(hand.door),
        lights = nullableList(hand.light),
        missiles = nullableList(hand.missile),
        players = nullableList(hand.player),
        spirits = nullableList(hand.spirit)
    )

fun toDeck(hands: List<Hand>): Deck =
    hands.fold(Deck(), { d, h -> d.plus(toDeck(h)) })

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val table: Tables = toTables(deck)
) {
  val bodyTable: IdentityMap<Body> get() = table.bodies
  val characterTable: IdentityMap<Character> get() = table.characters

  val players: List<Player>
    get() = deck.players

  val characters: Collection<Character>
    get() = deck.characters

  val bodies: Collection<Body>
    get() = deck.bodies

  val missiles: Collection<Missile>
    get() = deck.missiles

  val spirits: Collection<Spirit>
    get() = deck.spirits
}

fun addDeck(world: World, deck: Deck, nextId: IdSource): World {
  val newDeck = world.deck.plus(deck)
  return world.copy(
      deck = newDeck,
      nextId = nextId()
  )
}
