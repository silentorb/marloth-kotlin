package simulation.main

import silentorb.mythic.accessorize.Accessory
import simulation.entities.Attachment
import silentorb.mythic.accessorize.Modifier
import silentorb.mythic.aura.Sound
import silentorb.mythic.ent.*
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import silentorb.mythic.combat.general.ResourceBundle
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.combat.general.Destructible
import silentorb.mythic.combat.spatial.Missile
import silentorb.mythic.entities.Attributes
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.Performance
import silentorb.mythic.scenery.Light
import silentorb.mythic.timing.FloatCycle
import silentorb.mythic.timing.FloatTimer
import silentorb.mythic.timing.IntCycle
import silentorb.mythic.timing.IntTimer
import simulation.combat.PlayerOverlay
import simulation.intellect.assessment.Knowledge

// Deck is basically a database full of tables

data class Deck(
    val accessories: Table<Accessory> = mapOf(),
    val actions: Table<Action> = mapOf(),
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<CharacterAnimation> = mapOf(),
    val attachments: Table<Attachment> = mapOf(),
    val attributes: Table<Attributes> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val modifiers: Table<Modifier> = mapOf(),
    val characterRigs: Table<CharacterRig> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionShapes: Table<CollisionObject> = mapOf(),
    val cyclesFloat: Table<FloatCycle> = mapOf(),
    val cyclesInt: Table<IntCycle> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val destructibles: Table<Destructible> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val interactables: Table<Interactable> = mapOf(),
    val knowledge: Table<Knowledge> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val performances: Table<Performance> = mapOf(),
    val players: Table<Player> = mapOf(),
    val playerOverlays: Table<PlayerOverlay> = mapOf(),
    val resources: Table<ResourceBundle> = mapOf(),
    val respawnCountdowns: Table<RespawnCountdown> = mapOf(),
    val sounds: Table<Sound> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val timersFloat: Table<FloatTimer> = mapOf(),
    val timersInt: Table<IntTimer> = mapOf(),
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
