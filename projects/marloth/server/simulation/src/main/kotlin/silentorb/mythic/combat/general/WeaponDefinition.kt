package silentorb.mythic.combat.general

import silentorb.mythic.aura.SoundType

data class DamageDefinition(
    val type: DamageType,
    val amount: Int
)

data class WeaponDefinition(
    val damages: List<DamageDefinition>,
    val sound: SoundType? = null
)
