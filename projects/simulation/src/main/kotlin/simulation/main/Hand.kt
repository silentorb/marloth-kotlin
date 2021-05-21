package simulation.main

import silentorb.mythic.aura.Sound
import silentorb.mythic.characters.rigs.CharacterRig
import silentorb.mythic.characters.rigs.ThirdPersonRig
import silentorb.mythic.ent.AnyGraph
import silentorb.mythic.ent.GenericIdHand
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
import simulation.accessorize.Accessory
import simulation.accessorize.ItemPickup
import simulation.characters.Character
import simulation.combat.PlayerOverlay
import simulation.combat.general.Destructible
import simulation.combat.spatial.Missile
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.intellect.assessment.Knowledge

// Hand is a slice of a deck, mostly used for instantiating new entities.

typealias GetAnyGraph = (Any) -> AnyGraph

@Deprecated("Use NewHand")
data class Hand(
    val accessory: Accessory? = null,
    val action: Action? = null,
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val attributes: Attributes? = null,
    val body: Body? = null,
    val animation: CharacterAnimation? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val characterRig: CharacterRig? = null,
    val collisionShape: CollisionObject? = null,
    val cycleFloat: FloatCycle? = null,
    val depiction: Depiction? = null,
    val destructible: Destructible? = null,
    val door: Door? = null,
    val interactable: Interactable? = null,
    val itemPickup: ItemPickup? = null,
    val knowledge: Knowledge? = null,
    val light: Light? = null,
    val missile: Missile? = null,
    val particleEffect: ParticleEffect? = null,
    val performance: Performance? = null,
    val player: Player? = null,
    val playerOverlay: PlayerOverlay? = null,
//    val resources: ResourceBundle? = null,
    val respawnCountdown: RespawnCountdown? = null,
    val sound: Sound? = null,
    val spinner: Spinner? = null,
    val spirit: Spirit? = null,
    val thirdPersonRig: ThirdPersonRig? = null,
    val timerInt: IntTimer? = null,
    val timerFloat: FloatTimer? = null,
    val trigger: Trigger? = null,
    val ware: Ware? = null
)

@Deprecated("Use NewHand")
typealias IdHand = GenericIdHand<Hand>
