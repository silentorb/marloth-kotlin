package silentorb.mythic.combat.general

import silentorb.mythic.aura.SoundType

data class DamageDefinition(
    val type: DamageType,
    val amount: Int
)

data class WeaponDefinition(
    val attackMethod: AttackMethod,
    val damages: List<DamageDefinition>,
    val damageRadius: Float = 0f,
    val damageFalloff: Float = 0f,
    val sound: SoundType? = null
)
