package simulation.main

import silentorb.mythic.aura.Sound
import silentorb.mythic.characters.rigs.CharacterRig
import silentorb.mythic.characters.rigs.ThirdPersonRig
import silentorb.mythic.ent.*
import silentorb.mythic.entities.Attributes
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.Performance
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Light
import silentorb.mythic.timing.FloatCycle
import silentorb.mythic.timing.FloatTimer
import silentorb.mythic.timing.IntTimer
import simulation.accessorize.AccessoryStack
import simulation.accessorize.ItemPickup
import simulation.characters.Character
import simulation.combat.PlayerOverlay
import simulation.combat.general.Destructible
import simulation.combat.spatial.Missile
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.navigation.NavigationDirection
import simulation.misc.NodeReference
import simulation.updating.applyHands
import simulation.updating.finalizeHands

// Deck is basically a database full of tables

data class Deck(
    val accessories: Table<AccessoryStack> = mapOf(),
    val actions: Table<Action> = mapOf(),
    val ambientSounds: Table<AmbientAudioEmitter> = mapOf(),
    val animations: Table<CharacterAnimation> = mapOf(),
    val attributes: Table<Attributes> = mapOf(),
    val bodies: Table<Body> = mapOf(),
    val characterRigs: Table<CharacterRig> = mapOf(),
    val characters: Table<Character> = mapOf(),
    val collisionObjects: Table<CollisionObject> = mapOf(),
    val contracts: Table<Contract> = mapOf(),
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
    val nodeReferences: Table<NodeReference> = mapOf(),
    val particleEffects: Table<ParticleEffect> = mapOf(),
    val performances: Table<Performance> = mapOf(),
    val players: Table<Player> = mapOf(),
    val playerOverlays: Table<PlayerOverlay> = mapOf(),
    val respawnCountdowns: Table<RespawnCountdown> = mapOf(),
    val sounds: Table<Sound> = mapOf(),
    val spinners: Table<Spinner> = mapOf(),
    val spirits: Table<Spirit> = mapOf(),
    val targets: Table<Id> = mapOf(),
    val thirdPersonRigs: Table<ThirdPersonRig> = mapOf(),
    val timersFloat: Table<FloatTimer> = mapOf(),
    val timersInt: Table<IntTimer> = mapOf(),
    val triggers: Table<Trigger> = mapOf(),
)

fun allHandsToDeck(nextId: IdSource, newHands: List<NewHand>, time: Long = -1L, deck: Deck): Deck {
  val hands = newHands.flatMap(finalizeHands(nextId))
  return deck.copy(
      accessories = deck.accessories + applyHands(hands),
      actions = deck.actions + applyHands(hands),
      ambientSounds = deck.ambientSounds + applyHands(hands),
      animations = deck.animations + applyHands(hands),
      attributes = deck.attributes + applyHands(hands),
      bodies = deck.bodies + applyHands(hands),
      characterRigs = deck.characterRigs + applyHands(hands),
      characters = deck.characters + applyHands(hands),
      collisionObjects = deck.collisionObjects + applyHands(hands),
      contracts = deck.contracts + applyHands<Contract>(hands).mapValues { it.value.copy(start = time) },
      cyclesFloat = deck.cyclesFloat + applyHands(hands),
      depictions = deck.depictions + applyHands(hands),
      destructibles = deck.destructibles + applyHands(hands),
      doors = deck.doors + applyHands(hands),
      dynamicBodies = deck.dynamicBodies + applyHands(hands),
      interactables = deck.interactables + applyHands(hands),
      itemPickups = deck.itemPickups + applyHands(hands),
      knowledge = deck.knowledge + applyHands(hands),
      lights = deck.lights + applyHands(hands),
      missiles = deck.missiles + applyHands(hands),
      navigationDirections = deck.navigationDirections + applyHands(hands),
      nodeReferences = deck.nodeReferences + applyHands(hands),
      particleEffects = deck.particleEffects + applyHands(hands),
      performances = deck.performances + applyHands(hands),
      players = deck.players + applyHands(hands),
      playerOverlays = deck.playerOverlays + applyHands(hands),
      respawnCountdowns = deck.respawnCountdowns + applyHands(hands),
      sounds = deck.sounds + applyHands(hands),
      spinners = deck.spinners + applyHands(hands),
      spirits = deck.spirits + applyHands(hands),
      targets = deck.targets + applyHands(hands),
      thirdPersonRigs = deck.thirdPersonRigs + applyHands(hands),
      timersFloat = deck.timersFloat + applyHands(hands),
      timersInt = deck.timersInt + applyHands(hands),
      triggers = deck.triggers + applyHands(hands),
  )
}

val deckReflection = newDeckReflection(Deck::class, Hand::class)

val mergeDecks = genericMergeDecks(deckReflection)
val idHandsToDeck = genericIdHandsToDeck(deckReflection)
val removeEntities = genericRemoveEntities(deckReflection)

fun addEntitiesToWorldDeck(world: World, transform: (IdSource) -> List<NewHand>): World {
  val nextId = newIdSource(world.nextId)
  val hands = transform(nextId)
  return world.copy(
      deck = allHandsToDeck(nextId, hands, world.step, world.deck),
      nextId = nextId()
  )
}

typealias DeckSource = (IdSource) -> Deck
