package simulation

import simulation.evention.Trigger
import simulation.intellect.Spirit
import mythic.ent.Id
import simulation.physics.Body
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody
import simulation.old.Missile
import simulation.particles.ParticleEffect

data class Hand(
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val body: Body? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val collisionShape: CollisionObject? = null,
    val animation: ArmatureAnimation? = null,
    val depiction: Depiction? = null,
    val door: Door? = null,
    val item: Item? = null,
    val light: Light? = null,
    val missile: Missile? = null,
    val particleEffect: ParticleEffect? = null,
    val player: Player? = null,
    val spirit: Spirit? = null,
    val trigger: Trigger? = null,
    val interactable: Interactable? = null
)

data class IdHand(
    val id: Id,
    val hand: Hand
)
