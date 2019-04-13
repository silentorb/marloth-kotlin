package simulation

import colliding.Shape
import intellect.Spirit
import mythic.ent.Id
import physics.Body
import physics.DynamicBody

data class Hand(
    val id: Id,
    val ambientAudioEmitter: AmbientAudioEmitter? = null,
    val body: Body? = null,
    val dynamicBody: DynamicBody? = null,
    val character: Character? = null,
    val collisionShape: Shape? = null,
    val animation: ArmatureAnimation? = null,
    val depiction: Depiction? = null,
    val door: Door? = null,
    val item: Item? = null,
    val light: Light? = null,
    val missile: Missile? = null,
    val player: Player? = null,
    val spirit: Spirit? = null,
    val interactable: Interactable? = null
)
