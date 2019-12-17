package simulation.main

import silentorb.mythic.ent.*
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.misc.ResourceBundle
import simulation.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.characters.CharacterRig

// Deck is basically a database full of tables

data class Deck(
    val accessories: Table<Accessory> = mapOf(),
    val actions: Table<Action> = mapOf(),
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<CharacterAnimation> = mapOf(),
    val architecture: Table<ArchitectureElement> = mapOf(),
    val attachments: Table<Attachment> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val buffs: Table<Modifier> = mapOf(),
    val characterRigs: Table<CharacterRig> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionShapes: Table<CollisionObject> = mapOf(),
    val cycles: Table<Cycle> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val destructibles: Table<Destructible> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val interactables: Table<Interactable> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val performances: Table<Performance> = mapOf(),
    val players: Table<Player> = mapOf(),
    val resources: Table<ResourceBundle> = mapOf(),
    val respawnCountdowns: Table<RespawnCountdown> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val timers: Table<Timer> = mapOf(),
    val timersFloat: Table<FloatTimer> = mapOf(),
    val triggers: Table<Trigger> = mapOf(),
    val wares: Table<Ware> = mapOf()
)

val deckReflection = newDeckReflection(Deck::class, Hand::class)

val handToDeck = genericHandToDeck(deckReflection)
val mergeDecks = genericMergeDecks(deckReflection)
val allHandsOnDeck = genericAllHandsOnDeck(deckReflection)
val idHandsToDeck = genericIdHandsToDeck(deckReflection)
val removeEntities = genericRemoveEntities(deckReflection)

fun addEntitiesToWorldDeck(world: World, transform: (IdSource) -> List<IdHand>): World {
  val (nextId, finalize) = newIdSource(world)
  val hands = transform(nextId)
  return finalize(world.copy(
      deck = mergeDecks(world.deck, idHandsToDeck(hands))
  ))
}

fun pipeHandsToDeck(nextId: IdSource, sources: List<(Deck) -> List<Hand>>): (Deck) -> Deck = { deck ->
  pipe2(sources.map { handSource ->
    { accumulator: Deck -> allHandsOnDeck(handSource(accumulator), nextId, accumulator) }
  })(deck)
}

fun pipeIdHandsToDeck(sources: List<(Deck) -> List<IdHand>>): (Deck) -> Deck = { deck ->
  pipe2(sources.map { handSource ->
    { accumulator: Deck -> mergeDecks(idHandsToDeck(handSource(accumulator)), accumulator) }
  })(deck)
}

typealias DeckSource = (IdSource) -> Deck

fun resolveDecks(nextId: IdSource, deckSources: List<DeckSource>): List<Deck> =
    deckSources.map { it(nextId) }
