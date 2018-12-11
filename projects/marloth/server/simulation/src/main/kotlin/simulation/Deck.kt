package simulation

import intellect.Spirit
import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.entityMap
import physics.Body

typealias Table<T> = Map<Id, T>

fun <T> mapTable(table: Table<T>, action: (T) -> T): Table<T> =
    table.mapValues { (_, value) -> action(value) }

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
    val animations: Table<ArmatureAnimation> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val factions: Table<Faction> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val players: Table<Player> = mapOf(),
    val spirits: Table<Spirit> = mapOf()
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

//data class Tables(
//    val animations: IdentityMap<ArmatureAnimation>,
//    val bodies: IdentityMap<Body>,
//    val characters: IdentityMap<Character>,
//    val depictions: IdentityMap<Depiction>,
//    val doors: IdentityMap<Door>,
//    val missiles: IdentityMap<Missile>,
//    val spirits: IdentityMap<Spirit>
//)
//
//fun <T : Entity> entityMap2(list: Collection<T>): IdentityMap<T> =
//    IdentityMap(entityMap(list))

//fun toTables(deck: Deck): Tables =
//    Tables(
//        bodies = entityMap2(deck.bodies),
//        characters = entityMap2(deck.characters),
//        animations = entityMap2(deck.animations),
//        depictions = entityMap2(deck.depictions),
//        doors = entityMap2(deck.doors),
//        missiles = entityMap2(deck.missiles),
//        spirits = entityMap2(deck.spirits)
//    )

fun <T : Entity> nullableList(entity: T?): Table<T> =
    if (entity == null)
      mapOf()
    else
      mapOf(entity.id to entity)

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
  val bodyTable: Table<Body> get() = deck.bodies
  val characterTable: Table<Character> get() = deck.characters

  val players: List<Player>
    get() = deck.players.values.toList()

  val characters: Collection<Character>
    get() = deck.characters.values

  val bodies: Collection<Body>
    get() = deck.bodies.values

  val missiles: Collection<Missile>
    get() = deck.missiles.values

  val spirits: Collection<Spirit>
    get() = deck.spirits.values
}

fun addDeck(world: World, deck: Deck, nextId: IdSource): World {
  val newDeck = world.deck.plus(deck)
  return world.copy(
      deck = newDeck,
      nextId = nextId()
  )
}
