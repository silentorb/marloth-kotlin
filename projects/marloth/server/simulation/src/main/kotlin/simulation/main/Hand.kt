package simulation.main

import silentorb.mythic.ent.Id
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.misc.ResourceBundle
import simulation.particles.ParticleEffect
import silentorb.mythic.physics.Body
import simulation.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import simulation.physics.CharacterRig

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
    val cycle: Cycle? = null,
    val depiction: Depiction? = null,
    val destructible: Destructible? = null,
    val door: Door? = null,
    val interactable: Interactable? = null,
    val light: Light? = null,
    val particleEffect: ParticleEffect? = null,
    val performance: Performance? = null,
    val player: Player? = null,
    val resources: ResourceBundle? = null,
    val spirit: Spirit? = null,
    val timer: Timer? = null,
    val timerFloat: FloatTimer? = null,
    val trigger: Trigger? = null,
    val ware: Ware? = null,

    // Utility child list that does not directly map to a deck
    val attachments: List<HandAttachment> = listOf()
)

data class HandAttachment(
    val category: AttachmentCategory,
    val index: Int = 0,
    val hand: Hand
)

data class IdHand(
    val id: Id,
    val hand: Hand
)
