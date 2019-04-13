package simulation

import colliding.Shape
import intellect.Spirit
import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.Table
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
    val items: Table<Item> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val players: Table<Player> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val interactables: Table<Interactable> = mapOf()
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
      items = items.plus(other.items),
      lights = lights.plus(other.lights),
      missiles = missiles.plus(other.missiles),
      players = players.plus(other.players),
      spirits = spirits.plus(other.spirits),
      interactables = interactables.plus(other.interactables)
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
        doors = nullableList(hand.id, hand.door),
        items = nullableList(hand.item),
        lights = nullableList(hand.id, hand.light),
        missiles = nullableList(hand.missile),
        players = nullableList(hand.player),
        spirits = nullableList(hand.spirit),
        interactables = nullableList(hand.id, hand.interactable)
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

fun removeEntities(deck: Deck, removeIds: List<Id>): Deck {
  val isActive = { id: Id -> !removeIds.contains(id) }

  return deck.copy(
      ambientSounds = deck.ambientSounds.filterKeys(isActive),
      animations = deck.animations.filterKeys(isActive),
      bodies = deck.bodies.filterKeys(isActive),
      characters = deck.characters.filterKeys(isActive),
      collisionShapes = deck.collisionShapes.filterKeys(isActive),
      depictions = deck.depictions.filterKeys(isActive),
      dynamicBodies = deck.dynamicBodies.filterKeys(isActive),
      doors = deck.doors.filterKeys(isActive),
      items = deck.items.filterKeys(isActive),
      lights = deck.lights.filterKeys(isActive),
      missiles = deck.missiles.filterKeys(isActive),
      players = deck.players.filterKeys(isActive),
      spirits = deck.spirits.filterKeys(isActive),
      interactables = deck.interactables.filterKeys(isActive)
  )
}