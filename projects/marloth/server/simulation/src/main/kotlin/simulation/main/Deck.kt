package simulation.main

import simulation.evention.Trigger
import simulation.intellect.Spirit
import mythic.ent.*
import simulation.physics.Body
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody
import simulation.combat.Missile
import simulation.misc.*
import simulation.particles.ParticleEffect

fun <T> mapTable(table: Table<T>, action: (Id, T) -> T): Table<T> =
    table.mapValues { (id, value) -> action(id, value) }

fun <T> mapTableValues(table: Table<T>, action: (T) -> T): Table<T> =
    table.mapValues { (_, value) -> action(value) }

fun <A, B> mapTableValues(table: Table<A>, secondTable: Table<B>, action: (B, A) -> A): Table<A> =
    table.mapValues { (id, value) -> action(secondTable[id]!!, value) }

data class Deck(
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<ArmatureAnimation> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionShapes: Table<CollisionObject> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val items: Table<Item> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val players: Table<Player> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val triggers: Table<Trigger> = mapOf(),
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
      particleEffects = particleEffects.plus(other.particleEffects),
      players = players.plus(other.players),
      spirits = spirits.plus(other.spirits),
      triggers = triggers.plus(other.triggers),
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

fun toDeck(hand: Hand, id: Id): Deck =
    Deck(
        ambientSounds = nullableList(hand.ambientAudioEmitter),
        animations = nullableList(hand.animation),
        bodies = nullableList(id, hand.body),
        characters = nullableList(hand.character),
        collisionShapes = nullableList(id, hand.collisionShape),
        depictions = nullableList(id, hand.depiction),
        dynamicBodies = nullableList(id, hand.dynamicBody),
        doors = nullableList(id, hand.door),
        items = nullableList(hand.item),
        lights = nullableList(id, hand.light),
        particleEffects = nullableList(id, hand.particleEffect),
        players = nullableList(hand.player),
        spirits = nullableList(hand.spirit),
        triggers = nullableList(id, hand.trigger),
        interactables = nullableList(id, hand.interactable)
    )

fun toDeck(hand: IdHand) = toDeck(hand.hand, hand.id)

fun toDeck(hands: List<IdHand>): Deck =
    hands.fold(Deck(), { d, h -> d.plus(toDeck(h)) })

fun allHandsOnDeck(hands: List<Hand>, nextId: IdSource): Deck =
    hands.fold(Deck(), { d, h -> d.plus(toDeck(h, nextId())) })

val addDeck: ((IdSource) -> List<IdHand>) -> WorldTransform = { deckSource ->
  { world ->
    val (nextId, finalize) = newIdSource(world)
    val newDeck = world.deck.plus(toDeck(deckSource(nextId)))
    finalize(world.copy(
        deck = newDeck
    ))
  }
}

fun addHands(hands: List<Hand>): WorldTransform =
    addDeck { nextId -> hands.map { IdHand(nextId(), it) } }

fun addDecks(deckSources: List<(IdSource) -> List<IdHand>>): WorldTransform {
  return pipe(deckSources.map(addDeck))
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
      particleEffects = deck.particleEffects.filterKeys(isActive),
      players = deck.players.filterKeys(isActive),
      spirits = deck.spirits.filterKeys(isActive),
      triggers = deck.triggers.filterKeys(isActive),
      interactables = deck.interactables.filterKeys(isActive)
  )
}

fun newIdSource(world: World): Pair<IdSource, (World) -> World> {
  var availableIds = world.availableIds
  var nextId = world.nextId
  return Pair({
    if (availableIds.any()) {
      val result = availableIds.last()
      availableIds = availableIds.minus(result)
      result
    } else {
      nextId++
    }
  }, { newWorld ->
    newWorld.copy(
        availableIds = availableIds,
        nextId = nextId
    )
  })
}
