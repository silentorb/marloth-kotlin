package simulation.main

import mythic.ent.*
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.misc.ResourceBundle
import simulation.particles.ParticleEffect
import simulation.physics.Body
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody

data class Deck(
    val accessories: Table<Accessory> = mapOf(),
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<ArmatureAnimation> = mapOf(),
    val architecture: Table<ArchitectureElement> = mapOf(),
    val attachments: Table<Attachment> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val buffs: Table<Modifier> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionShapes: Table<CollisionObject> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val destructibles: Table<Destructible> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val interactables: Table<Interactable> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val players: Table<Player> = mapOf(),
    val resources: Table<ResourceBundle> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val timers: Table<Timer> = mapOf(),
    val triggers: Table<Trigger> = mapOf(),
    val wares: Table<Ware> = mapOf()
)

fun <T> mapTable(table: Table<T>, action: (Id, T) -> T): Table<T> =
    table.mapValues { (id, value) -> action(id, value) }

fun <T> mapTableValues(table: Table<T>, action: (T) -> T): Table<T> =
    table.mapValues { (_, value) -> action(value) }

fun <A, B> mapTableValues(table: Table<A>, secondTable: Table<B>, action: (B, A) -> A): Table<A> =
    table.mapValues { (id, value) -> action(secondTable[id]!!, value) }

fun <T : WithId> nullableList(entity: T?): Table<T> =
    if (entity == null)
      mapOf()
    else
      mapOf(entity.id to entity)

fun <T> nullableList(id: Id, entity: T?): Table<T> =
    if (entity == null)
      mapOf()
    else
      mapOf(id to entity)

fun toDeck(hand: IdHand) = toDeck(hand.id, hand.hand)

fun toDeck(hands: List<IdHand>): Deck =
    hands.fold(Deck(), { d, h -> d.plus(toDeck(h)) })

fun mergeDecks(decks: List<Deck>): Deck =
    decks.reduce { a, deck -> a.plus(deck) }

fun allHandsOnDeck(hands: List<Hand>, nextId: IdSource, deck: Deck = Deck()): Deck =
    hands.fold(deck, { d, h -> d.plus(toDeck(nextId, h)) })

val addDeck: ((IdSource) -> List<IdHand>) -> WorldTransform = { deckSource ->
  { world ->
    val (nextId, finalize) = newIdSource(world)
    val newDeck = world.deck.plus(toDeck(deckSource(nextId)))
    finalize(world.copy(
        deck = newDeck
    ))
  }
}

fun pipeHandsToDeck(nextId: IdSource, sources: List<(Deck) -> List<Hand>>): (Deck) -> Deck = { deck ->
  pipe(sources.map { handSource ->
    { accumulator: Deck -> allHandsOnDeck(handSource(accumulator), nextId, accumulator) }
  })(deck)
}

fun addHands(hands: List<Hand>): WorldTransform =
    addDeck { nextId -> hands.map { IdHand(nextId(), it) } }

fun addDecks(deckSources: List<(IdSource) -> List<IdHand>>): WorldTransform {
  return pipe(deckSources.map(addDeck))
}

typealias DeckSource = (IdSource) -> Deck

fun resolveDecks(nextId: IdSource, deckSources: List<DeckSource>): List<Deck> =
    deckSources.map { it(nextId) }
