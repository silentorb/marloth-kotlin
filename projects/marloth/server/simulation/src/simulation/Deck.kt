package simulation

import intellect.Spirit
import physics.Body
import simulation.combat.Missile

val maxPlayerCount = 4

typealias Players = List<Player>
typealias IdentityMap<T> = Map<Id, T>
typealias BodyTable = Map<Id, Body>
typealias CharacterTable = Map<Id, Character>

data class Hand(
    val body: Body? = null,
    val character: Character? = null,
    val animation: ArmatureAnimation? = null,
    val faction: Faction? = null,
    val furnishing: Furnishing? = null,
    val missile: Missile? = null,
    val player: Player? = null,
    val spirit: Spirit? = null
)

data class Deck(
    val animations: List<ArmatureAnimation> = listOf(),
    val bodies: List<Body> = listOf(),
    val characters: List<Character> = listOf(),
    val factions: List<Faction> = listOf(),
    val furnishings: List<Furnishing> = listOf(),
    val missiles: List<Missile> = listOf(),
    val players: List<Player> = listOf(),
    val spirits: List<Spirit> = listOf()
) {
  fun plus(other: Deck) = this.copy(
      animations = animations.plus(other.animations),
      bodies = bodies.plus(other.bodies),
      characters = characters.plus(other.characters),
      factions = factions.plus(other.factions),
      furnishings = furnishings.plus(other.furnishings),
      missiles = missiles.plus(other.missiles),
      players = players.plus(other.players),
      spirits = spirits.plus(other.spirits)
  )
}

data class Tables(
    val animationTable: IdentityMap<ArmatureAnimation>,
    val bodyTable: BodyTable,
    val characterTable: CharacterTable,
    val furnishingTable: IdentityMap<Furnishing>,
    val missileTable: Map<Id, Missile>,
    val spiritTable: Map<Id, Spirit>
)

fun toTables(deck: Deck): Tables =
    Tables(
        bodyTable = mapEntities(deck.bodies),
        characterTable = mapEntities(deck.characters),
        animationTable = mapEntities(deck.animations),
        furnishingTable = mapEntities(deck.furnishings),
        missileTable = mapEntities(deck.missiles),
        spiritTable = mapEntities(deck.spirits)
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
        factions = nullableList(hand.faction),
        furnishings = nullableList(hand.furnishing),
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
  val tables: Tables = toTables(deck)
  val animationTable: IdentityMap<ArmatureAnimation> get() = tables.animationTable
  val bodyTable: BodyTable get() = tables.bodyTable
  val characterTable: CharacterTable get() = tables.characterTable
  val furnishingTable: IdentityMap<Furnishing> get() = tables.furnishingTable
  val missileTable: Map<Id, Missile> get() = tables.missileTable
  val spiritTable: Map<Id, Spirit> get() = tables.spiritTable

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

fun <T : EntityLike> mapEntities(list: Collection<T>): Map<Id, T> =
    list.associate { Pair(it.id, it) }

fun addDeck(world: World, deck: Deck, nextId: IdSource): World {
  val newDeck = world.deck.plus(deck)
  return world.copy(
      deck = newDeck,
      nextId = nextId()
  )
}