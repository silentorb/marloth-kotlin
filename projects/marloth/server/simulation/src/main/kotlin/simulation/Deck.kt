package simulation

import colliding.Shape
import intellect.Spirit
import mythic.ent.*
import physics.Body
import physics.DynamicBody
import randomly.Dice

fun <T> mapTable(table: Table<T>, action: (Id, T) -> T): Table<T> =
    table.mapValues { (id, value) -> action(id, value) }

fun <T> mapTableValues(table: Table<T>, action: (T) -> T): Table<T> =
    table.mapValues { (_, value) -> action(value) }

data class Deck(
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<ArmatureAnimation> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionShapes: Table<Shape> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val factions: Table<Faction> = mapOf(),
    val items: Table<Item> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val players: Table<Player> = mapOf(),
    val spirits: Table<Spirit> = mapOf()
) {
  fun plus(other: Deck) = this.copy(
      ambientSounds = ambientSounds.plus(other.ambientSounds),
      animations = animations.plus(other.animations),
      bodies = bodies.plus(other.bodies),
      characters = characters.plus(other.characters),
      collisionShapes = collisionShapes.plus(other.collisionShapes),
      depictions = depictions.plus(other.depictions),
      doors = doors.plus(other.doors),
      dynamicBodies = dynamicBodies.plus(other.dynamicBodies),
      factions = factions.plus(other.factions),
      items = items.plus(other.items),
      lights = lights.plus(other.lights),
      missiles = missiles.plus(other.missiles),
      players = players.plus(other.players),
      spirits = spirits.plus(other.spirits)
  )
}

fun <T : Entity> nullableList(entity: T?): Table<T> =
    if (entity == null)
      mapOf()
    else
      mapOf(entity.id to entity)

fun <T> nullableList(id: Id, entity: T?): Table<T> =
    if (entity == null)
      mapOf()
    else
      mapOf(id to entity)

fun toDeck(hand: Hand): Deck =
    Deck(
        ambientSounds = nullableList(hand.ambientAudioEmitter),
        animations = nullableList(hand.animation),
        bodies = nullableList(hand.body),
        characters = nullableList(hand.character),
        collisionShapes = nullableList(hand.id, hand.collisionShape),
        depictions = nullableList(hand.id, hand.depiction),
        dynamicBodies = nullableList(hand.id, hand.dynamicBody),
        doors = nullableList(hand.door),
        items = nullableList(hand.item),
        lights = nullableList(hand.id, hand.light),
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
    val dice: Dice,
    val gameOver: GameOver? = null
) {
  val bodyTable: Table<Body> get() = deck.bodies
  val characterTable: Table<Character> get() = deck.characters

  val players: List<Player>
    get() = deck.players.values.toList()

  val characters: Collection<Character>
    get() = deck.characters.values

  val bodies: Collection<Body>
    get() = deck.bodies.values
}

typealias WorldPair = Pair<World, World>

fun addDeck(world: World, deck: Deck, nextId: IdSource): World {
  val newDeck = world.deck.plus(deck)
  return world.copy(
      deck = newDeck,
      nextId = nextId()
  )
}
