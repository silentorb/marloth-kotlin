package simulation.main

import simulation.accessorize.Accessory
import simulation.accessorize.ItemPickup
import simulation.entities.Attachment
import silentorb.mythic.aura.Sound
import silentorb.mythic.ent.*
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.combat.general.ResourceBundle
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.characters.rigs.CharacterRig
import silentorb.mythic.characters.rigs.ThirdPersonRig
import simulation.combat.general.Destructible
import simulation.combat.spatial.Missile
import silentorb.mythic.entities.Attributes
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.Performance
import silentorb.mythic.scenery.Light
import silentorb.mythic.timing.FloatCycle
import silentorb.mythic.timing.FloatTimer
import silentorb.mythic.timing.IntTimer
import simulation.characters.Character
import simulation.combat.PlayerOverlay
import simulation.intellect.assessment.Knowledge
import simulation.intellect.navigation.NavigationDirection

// Deck is basically a database full of tables

data class Deck(
    val accessories: Table<Accessory> = mapOf(),
    val actions: Table<Action> = mapOf(),
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<CharacterAnimation> = mapOf(),
    val attachments: Table<Attachment> = mapOf(),
    val attributes: Table<Attributes> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val characterRigs: Table<CharacterRig> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionObjects: Table<CollisionObject> = mapOf(),
    val cyclesFloat: Table<FloatCycle> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val destructibles: Table<Destructible> = mapOf(),
    val doors: Table<Door> = mapOf(),
    val dynamicBodies: Table<DynamicBody> = mapOf(),
    val interactables: Table<Interactable> = mapOf(),
    val itemPickups: Table<ItemPickup> = mapOf(),
    val knowledge: Table<Knowledge> = mapOf(),
    val lights: Table<Light> = mapOf(),
    val missiles: Table<Missile> = mapOf(),
    val navigationDirections: Table<NavigationDirection> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val performances: Table<Performance> = mapOf(),
    val players: Table<Player> = mapOf(),
    val playerOverlays: Table<PlayerOverlay> = mapOf(),
//    val resources: Table<ResourceBundle> = mapOf(),
    val respawnCountdowns: Table<RespawnCountdown> = mapOf(),
    val sounds: Table<Sound> = mapOf(),
    val spinners: Table<Spinner> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val targets: Table<Id> = mapOf(),
    val thirdPersonRigs: Table<ThirdPersonRig> = mapOf(),
    val timersFloat: Table<FloatTimer> = mapOf(),
    val timersInt: Table<IntTimer> = mapOf(),
    val triggers: Table<Trigger> = mapOf(),
    val wares: Table<Ware> = mapOf(),
)

val deckReflection = newDeckReflection(Deck::class, Hand::class)

val mergeDecks = genericMergeDecks(deckReflection)
val idHandsToDeck = genericIdHandsToDeck(deckReflection)
val removeEntities = genericRemoveEntities(deckReflection)

fun addEntitiesToWorldDeck(world: World, transform: (IdSource) -> List<IdHand>): World {
  val nextId = newIdSource(world.nextId)
  val hands = transform(nextId)
  return world.copy(
      deck = mergeDecks(world.deck, idHandsToDeck(hands)),
      nextId = nextId()
  )
}

typealias DeckSource = (IdSource) -> Deck
