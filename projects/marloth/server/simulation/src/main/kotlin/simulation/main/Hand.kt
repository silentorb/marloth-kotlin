package simulation.main

import mythic.ent.Id
import simulation.entities.*
import simulation.happenings.Trigger
import simulation.intellect.Spirit
import simulation.misc.ResourceBundle
import simulation.particles.ParticleEffect
import simulation.physics.Body
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody

data class Hand(
    val accessory: Accessory? = null,
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val body: Body? = null,
    val buff: Modifier? = null,
    val animation: ArmatureAnimation? = null,
    val architecture: ArchitectureElement? = null,
    val attachment: Attachment? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val collisionShape: CollisionObject? = null,
    val depiction: Depiction? = null,
    val destructible: Destructible? = null,
    val door: Door? = null,
    val interactable: Interactable? = null,
    val light: Light? = null,
    val particleEffect: ParticleEffect? = null,
    val player: Player? = null,
    val resources: ResourceBundle? = null,
    val spirit: Spirit? = null,
    val timer: Timer? = null,
    val trigger: Trigger? = null,
    val ware: Ware? = null,

    // Utility child list that does not directly map to a deck
    val attachments: List<HandAttachment> = listOf()
)

data class HandAttachment(
    val category: AttachmentTypeId,
    val index: Int = 0,
    val hand: Hand
)

data class IdHand(
    val id: Id,
    val hand: Hand
)
