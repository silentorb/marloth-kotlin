package simulation.main

import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.misc.ResourceBundle
import simulation.particles.ParticleEffect
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.ent.GenericIdHand
import silentorb.mythic.scenery.Light

// Hand is a slice of a deck, mostly used for instantiating new entities.

data class Hand(
    val accessory: Accessory? = null,
    val action: Action? = null,
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val body: Body? = null,
    val buff: Modifier? = null,
    val animation: CharacterAnimation? = null,
    val architecture: ArchitectureElement? = null,
    val attachment: Attachment? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val characterRig: CharacterRig? = null,
    val collisionShape: CollisionObject? = null,
    val cycleFloat: FloatCycle? = null,
    val cycleInt: IntCycle? = null,
    val depiction: Depiction? = null,
    val destructible: Destructible? = null,
    val door: Door? = null,
    val interactable: Interactable? = null,
    val light: Light? = null,
    val particleEffect: ParticleEffect? = null,
    val performance: Performance? = null,
    val player: Player? = null,
    val resources: ResourceBundle? = null,
    val respawnCountdown: RespawnCountdown? = null,
    val spirit: Spirit? = null,
    val timer: Timer? = null,
    val timerFloat: FloatTimer? = null,
    val trigger: Trigger? = null,
    val ware: Ware? = null
)

typealias IdHand = GenericIdHand<Hand>
