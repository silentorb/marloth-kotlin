package simulation

import intellect.Spirit
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.entityMap
import physics.Body

typealias Players = List<Player>
typealias IdentityMap<T> = Map<Id, T>
typealias BodyTable = Map<Id, Body>
typealias CharacterTable = Map<Id, Character>

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
    val bodies: BodyTable,
    val characters: CharacterTable,
    val depictions: IdentityMap<Depiction>,
    val doors: IdentityMap<Door>,
    val missiles: Map<Id, Missile>,
    val spirits: Map<Id, Spirit>
)

fun toTables(deck: Deck): Tables =
    Tables(
        bodies = entityMap(deck.bodies),
        characters = entityMap(deck.characters),
        animations = entityMap(deck.animations),
        depictions = entityMap(deck.depictions),
        doors = entityMap(deck.doors),
        missiles = entityMap(deck.missiles),
        spirits = entityMap(deck.spirits)
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
    val deck: Deck
) {
  val table: Tables = toTables(deck)
  val bodyTable: BodyTable get() = table.bodies
  val characterTable: CharacterTable get() = table.characters

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
