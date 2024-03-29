package simulation.combat.general

import silentorb.mythic.aura.SoundType
import silentorb.mythic.scenery.MeshName

data class DamageDefinition(
    val type: DamageType,
    val amount: Int
)

data class WeaponDefinition(
    val attackMethod: AttackMethod,
    val damages: List<DamageDefinition>,
    val damageRadius: Float = 0f,
    val velocity: Float = 1f,
    val damageFalloff: Float = 0f,
    val sound: SoundType? = null,
    val impulse: Float = 0f,
    val missileMesh: MeshName? = null,
    val yawOffset: Float = 0f,
)
