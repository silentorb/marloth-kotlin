package simulation.main

import mythic.ent.Id
import simulation.evention.Trigger
import simulation.intellect.Spirit
import simulation.misc.*
import simulation.particles.ParticleEffect
import simulation.physics.Body
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody

data class Hand(
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val body: Body? = null,
    val buff: Buff? = null,
    val attachment: Attachment? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val collisionShape: CollisionObject? = null,
    val animation: ArmatureAnimation? = null,
    val depiction: Depiction? = null,
    val door: Door? = null,
    val entity: Entity? = null,
    val interactable: Interactable? = null,
    val light: Light? = null,
    val particleEffect: ParticleEffect? = null,
    val player: Player? = null,
    val spirit: Spirit? = null,
    val timer: Timer? = null,
    val trigger: Trigger? = null
)

data class IdHand(
    val id: Id,
    val hand: Hand
)
